package com.safetyapp.mainapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.*;

public class FileviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout /* previewLayout,*/ buttonLayout;
    private ScrollView previewScroll;
    private ImageView previewImage;
    private TextView previewTitle;
    @SuppressWarnings("FieldCanBeLocal")
    private Button buttonAddTags, buttonDeleteFile;

    private FirebaseStorage storage;
    private DatabaseReference dbRef;

    private String selectedFileName;
    private String selectedFileUrl;
    private String lastUploadedFileId;

    @SuppressWarnings("FieldMayBeFinal")
    private List<String> allFileNames = new ArrayList<>(); // All filenames

    @SuppressWarnings("FieldMayBeFinal")
    private Map<String, List<String>> allTags = new HashMap<>(); // fileId -> tags

    @SuppressWarnings("FieldMayBeFinal")
    private Set<String> activeTagFilters = new HashSet<>();

    private FileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileview);

        // UI references
        recyclerView = findViewById(R.id.recyclerViewFiles);
        //previewLayout = findViewById(R.id.previewLayout);
        previewScroll = findViewById(R.id.previewScroll);
        buttonLayout = findViewById(R.id.buttonLayout);
        previewImage = findViewById(R.id.previewImage);
        previewTitle = findViewById(R.id.previewTitle);
        buttonAddTags = findViewById(R.id.buttonAddTags);
        buttonDeleteFile = findViewById(R.id.buttonDeleteFile);

        // Firebase
        storage = FirebaseStorage.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), allTags, this::onFileSelected);
        recyclerView.setAdapter(adapter);

        // Load files and tags
        loadFileTitlesAndTags();

        // Set up tag filter bar
        setupTagFilterBar();

        // Button actions
        buttonAddTags.setOnClickListener(v -> showTagPopup());
        buttonDeleteFile.setOnClickListener(v -> deleteSelectedFile());
    }

    // Loads file names from storage and their tags from database
    private void loadFileTitlesAndTags() {
        StorageReference filesRef = storage.getReference().child("uploads");

        filesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    allFileNames.clear();
                    allTags.clear();

                    for (StorageReference item : listResult.getItems()) {
                        allFileNames.add(item.getName());
                    }

                    // Load tags from Firebase DB
                    dbRef.child("documents").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot doc : snapshot.getChildren()) {
                                List<String> tags = new ArrayList<>();
                                for (DataSnapshot tagSnap : doc.child("tags").getChildren()) {
                                    tags.add(tagSnap.getValue(String.class));
                                }
                                allTags.put(doc.getKey(), tags); // fileId = fileName.replace(".", "_")
                            }
                            applyTagFilter(); // show filtered list
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(FileviewActivity.this, "Failed to load tags", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load files: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Called when user taps a file name
    private void onFileSelected(String fileName) {
        selectedFileName = fileName;
        lastUploadedFileId = fileName.replace(".", "_"); // Firebase-safe key

        StorageReference fileRef = storage.getReference().child("uploads/" + fileName);
        fileRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    selectedFileUrl = uri.toString();
                    previewTitle.setText(fileName);

                    if (fileName.endsWith(".pdf")) {
                        // Show static PDF icon + open intent
                        previewImage.setImageResource(R.drawable.ic_file);
                        previewImage.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(selectedFileUrl), "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        });
                    } else {
                        // Show image preview
                        Glide.with(this).load(uri).into(previewImage);
                        previewImage.setOnClickListener(null);
                    }

                    previewScroll.setVisibility(View.VISIBLE);
                    buttonLayout.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to preview file: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Delete selected file from Firebase Storage and DB
    private void deleteSelectedFile() {
        if (selectedFileName == null) return;

        StorageReference fileRef = storage.getReference().child("uploads/" + selectedFileName);
        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    dbRef.child("documents").child(lastUploadedFileId).removeValue();
                    allFileNames.remove(selectedFileName);
                    allTags.remove(lastUploadedFileId);
                    applyTagFilter();

                    previewScroll.setVisibility(View.GONE);
                    buttonLayout.setVisibility(View.GONE);
                    Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Filters visible files based on selected tags
    private void applyTagFilter() {
        List<String> filtered = new ArrayList<>();

        for (String fileName : allFileNames) {
            String fileId = fileName.replace(".", "_");
            List<String> tags = allTags.getOrDefault(fileId, new ArrayList<>());

            // If no filter is active OR tags match filter, show file
            if (activeTagFilters.isEmpty() || !Collections.disjoint(Objects.requireNonNull(tags), activeTagFilters)) {
                filtered.add(fileName);
            }
        }

        adapter = new FileAdapter(filtered, allTags, this::onFileSelected);
        recyclerView.setAdapter(adapter);
    }

    // Displays popup with fixed tags for user to select
    private void showTagPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_tag, null);
        builder.setView(view);

        GridLayout tagContainer = view.findViewById(R.id.tagContainer);

        // Define fixed tags + their colors
        Map<String, Integer> tagColors = new LinkedHashMap<>();
        tagColors.put("Identification", Color.RED);
        tagColors.put("Legal Documents", Color.BLUE);
        tagColors.put("Financial Statements", Color.GREEN);

        Set<String> selectedTags = new HashSet<>();

        for (String tag : tagColors.keySet()) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setTextColor(Color.WHITE);
            tagView.setTextSize(16);
            tagView.setPadding(32, 16, 32, 16);
            tagView.setGravity(Gravity.CENTER);
            tagView.setBackgroundResource(R.drawable.tag_background);
            tagView.setAlpha(0.5f);

            GradientDrawable bg = (GradientDrawable) tagView.getBackground();
            bg.setColor(tagColors.get(tag));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(12, 12, 12, 12);
            tagView.setLayoutParams(params);

            tagView.setOnClickListener(v -> {
                if (selectedTags.contains(tag)) {
                    selectedTags.remove(tag);
                    tagView.setAlpha(0.5f);
                } else {
                    selectedTags.add(tag);
                    tagView.setAlpha(1.0f);
                }
            });

            tagContainer.addView(tagView);
        }

        builder.setTitle("Select Tags");
        builder.setPositiveButton("Save", (dialog, which) -> saveTagsToFirebase(selectedTags));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Saves selected tags to DB under file's ID
    private void saveTagsToFirebase(Set<String> selectedTags) {
        if (lastUploadedFileId == null || lastUploadedFileId.isEmpty()) {
            Toast.makeText(this, "No uploaded file to tag", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tagsList = new ArrayList<>(selectedTags);

        dbRef.child("documents").child(lastUploadedFileId).child("tags").setValue(tagsList)
                .addOnSuccessListener(aVoid -> {
                    allTags.put(lastUploadedFileId, tagsList);
                    applyTagFilter();
                    Toast.makeText(this, "Tags saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save tags: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Initializes tag filter buttons (A, B, C)
    private void setupTagFilterBar() {
        GridLayout tagFilterLayout = findViewById(R.id.tagFilterLayout);
        String[] tags = {"Identification", "Legal Documents", "Financial Statements"};

        for (String tag : tags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setTextColor(Color.WHITE);
            tagView.setPadding(32, 16, 32, 16);
            tagView.setBackgroundResource(R.drawable.tag_background);
            tagView.setAlpha(0.5f);

            GradientDrawable bg = (GradientDrawable) tagView.getBackground();
            switch (tag) {
                case "Identification": bg.setColor(Color.RED); break;
                case "Legal Documents": bg.setColor(Color.BLUE); break;
                case "Financial Statements": bg.setColor(Color.GREEN); break;
            }

            tagView.setOnClickListener(v -> {
                if (activeTagFilters.contains(tag)) {
                    activeTagFilters.remove(tag);
                    tagView.setAlpha(0.5f);
                } else {
                    activeTagFilters.add(tag);
                    tagView.setAlpha(1.0f);
                }
                applyTagFilter();
            });

            tagFilterLayout.addView(tagView);
        }
    }
}
