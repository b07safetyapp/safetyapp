package com.safetyapp.mainapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadFragment extends Fragment implements FileAdapter.OnFileDeleteListener {

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private EditText tagInput;
    private Button uploadBtn;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileItem> fileItemList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tagInput = view.findViewById(R.id.tagInput);
        uploadBtn = view.findViewById(R.id.selectFileButton);
        recyclerView = view.findViewById(R.id.recyclerViewFiles);

        fileAdapter = new FileAdapter(fileItemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(fileAdapter);

        // Launch file picker and handle result
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            uploadFileToFirebase(fileUri);
                        }
                    }
                });

        uploadBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Please sign in to upload", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // or "image/*" or "application/pdf" if you want to restrict
            filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"));
        });

        listenToFiles();
    }

    /**
     * Listen to Firestore for changes in files collection
     * and update RecyclerView in real-time
     */
    private void listenToFiles() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Query only files uploaded by this user
        db.collection("files")
                .whereEqualTo("uploadedBy", user.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("Firestore", "Listen failed: " + e.getMessage());
                            return;
                        }

                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        FileItem newItem = dc.getDocument().toObject(FileItem.class);
                                        newItem.docId = dc.getDocument().getId();
                                        fileItemList.add(newItem);
                                        break;
                                    case REMOVED:
                                        String removedId = dc.getDocument().getId();
                                        for (int i = 0; i < fileItemList.size(); i++) {
                                            if (fileItemList.get(i).docId.equals(removedId)) {
                                                fileItemList.remove(i);
                                                break;
                                            }
                                        }
                                        break;
                                    case MODIFIED:
                                        // Optionally handle updates
                                        break;
                                }
                            }
                            fileAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * Uploads file to Firebase Storage with custom metadata
     * and saves metadata including tags to Firestore
     */
    private void uploadFileToFirebase(Uri fileUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please sign in to upload", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(requireContext().getContentResolver().getType(fileUri));
        String filename = System.currentTimeMillis() + (extension != null ? ("." + extension) : "");

        StorageReference fileRef = storage.getReference().child("uploads/" + filename);

        // Attach uploader UID as custom metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("uploadedBy", user.getUid())
                .build();

        fileRef.putFile(fileUri, metadata)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveFileMetadataToFirestore(uri.toString(), fileUri, user.getUid());
                            Toast.makeText(getContext(), "Upload successful!", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Saves the file metadata to Firestore including tags
     */
    private void saveFileMetadataToFirestore(String url, Uri fileUri, String uid) {
        String type = requireContext().getContentResolver().getType(fileUri);

        // Parse tags from EditText
        String rawTags = tagInput.getText().toString().trim();
        List<String> tags = new ArrayList<>();
        if (!rawTags.isEmpty()) {
            for (String tag : rawTags.split(",")) {
                tag = tag.trim();
                if (!tag.isEmpty()) tags.add(tag);
            }
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("url", url);
        metadata.put("type", type);
        metadata.put("uploadedBy", uid);
        metadata.put("timestamp", FieldValue.serverTimestamp());
        metadata.put("tags", tags);

        db.collection("files")
                .add(metadata)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Metadata saved"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving metadata: " + e.getMessage()));
    }

    /**
     * Delete file both from Storage and Firestore
     */
    @Override
    public void onDeleteClick(FileItem fileItem) {
        // Extract storage path from URL
        String storagePath = getStoragePathFromUrl(fileItem.url);
        StorageReference fileRef = storage.getReference().child(storagePath);

        fileRef.delete()
                .addOnSuccessListener(unused ->
                        db.collection("files").document(fileItem.docId).delete()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(getContext(), "File deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to delete metadata: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete file: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Helper method to decode Firebase Storage path from download URL
     */
    private String getStoragePathFromUrl(String url) {
        Uri uri = Uri.parse(url);
        String fullPath = uri.getPath(); // e.g. /v0/b/app-id/o/uploads%2Ffilename.jpg
        assert fullPath != null;
        int startIndex = fullPath.indexOf("/o/") + 3;
        int endIndex = fullPath.indexOf("?", startIndex);
        String encodedPath = fullPath.substring(startIndex, endIndex);
        return Uri.decode(encodedPath); // e.g. uploads/filename.jpg
    }
}
