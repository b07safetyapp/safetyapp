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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that allows user to select a file (image or PDF),
 * upload it to Firebase Storage, and store its download URL
 * and metadata in Firestore. Uploads require user to be signed in.
 */
public class UploadFragment extends Fragment {

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button uploadBtn = view.findViewById(R.id.selectFileButton);

        // Set up the file picker launcher using the modern Activity Result API
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            uploadFileToFirebase(fileUri); // Begin upload
                        }
                    }
                });

        // When button is clicked, open file picker
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimetypes = {"image/*", "application/pdf"};
            intent.setType("*/*"); // Accept any file type (use "image/*" or "application/pdf" to filter)
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"));
        });
    }

    /**
     * Uploads the selected file to Firebase Storage.
     * Only proceeds if user is signed in.
     */
    private void uploadFileToFirebase(Uri fileUri) {
        // Check if user is signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { // vro forgor to sign in
            Toast.makeText(getContext(), "Please sign in to upload", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // optional: clears backstack
            startActivity(intent);
            return;
        }

        // Get MIME type extension for proper filename
        String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(requireContext().getContentResolver().getType(fileUri));
        String filename = System.currentTimeMillis() + (extension != null ? ("." + extension) : "");

        // Reference to the file in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageRef.child("uploads/" + filename);

        // Upload the file
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        // Get download URL once upload is complete
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save the file's URL and metadata to Firestore
                            saveFileMetadataToFirestore(uri.toString(), fileUri, user.getUid());
                        }))
                .addOnFailureListener(e ->
                        Log.e("FirebaseUpload", "Upload failed: " + e.getMessage()));
    }

    /**
     * Saves file metadata and download URL to Firestore.
     */
    private void saveFileMetadataToFirestore(String url, Uri fileUri, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String type = requireContext().getContentResolver().getType(fileUri);

        // Construct metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("url", url);                   // Direct download URL
        metadata.put("type", type);                 // MIME type
        metadata.put("uploadedBy", uid);            // UID of user
        metadata.put("timestamp", FieldValue.serverTimestamp()); // Upload timestamp

        // Save to Firestore in the "files" collection
        db.collection("files")
                .add(metadata)
                .addOnSuccessListener(docRef ->
                        Log.d("Firestore", "Metadata saved"))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Failed to save metadata: " + e.getMessage()));
    }
}
