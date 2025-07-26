package com.safetyapp.mainapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import java.io.IOException;
import java.util.*;

public class UploadActivity extends BaseActivity {

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_upload;
    }
    // File Upload UI
    @SuppressWarnings("FieldCanBeLocal")
    CardView cardUpload;
    private TextView tvUploadStatus;
    @SuppressWarnings("FieldCanBeLocal")
    private Button btnAddTags, btnViewDocs;
    private ProgressBar progressBar;

    // Contact Fields
    private EditText etContactName, etContactRel, etContactPhone;
    @SuppressWarnings("FieldCanBeLocal")
    private Button btnAddContact, btnEditContacts;

    // Firebase
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    private Uri fileUri;

    // Store last uploaded file ID here to associate tags with it
    private String lastUploadedFileId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase refs
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        dbRef = FirebaseDatabase.getInstance().getReference();

        // UI
        cardUpload = findViewById(R.id.cardUpload);
        tvUploadStatus = findViewById(R.id.tvUploadStatus);
        btnAddTags = findViewById(R.id.btnAddTags);  // FIXED IDs here, use correct IDs!
        btnViewDocs = findViewById(R.id.btnViewDocs);
        progressBar = findViewById(R.id.progressBarUpload);

        cardUpload.setOnClickListener(v -> chooseFile());

        btnAddTags.setOnClickListener(v -> {
            if (lastUploadedFileId == null) {
                Toast.makeText(this, "Upload a file first to add tags", Toast.LENGTH_SHORT).show();
                return;
            }
            showTagPopup();
        });

        btnViewDocs.setOnClickListener(v -> startActivity(new Intent(this, FileviewActivity.class)));

        // Contacts
        etContactName = findViewById(R.id.etContactName);
        etContactRel = findViewById(R.id.etContactRel);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnEditContacts = findViewById(R.id.btnEditContacts);

        btnAddContact.setOnClickListener(v -> addContact());
        btnEditContacts.setOnClickListener(v -> startActivity(new Intent(this, ContactActivity.class)));
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        fileChooserLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> fileChooserLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    fileUri = result.getData().getData();
                    uploadFile();
                }
            });

    @SuppressLint("SetTextI18n")
    private void uploadFile() {
        if (fileUri == null) return;

        progressBar.setVisibility(View.VISIBLE); // show

        String ext = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(getContentResolver().getType(fileUri));
        String fileName = System.currentTimeMillis() + "." + ext;

        StorageReference fileRef = storageRef.child(fileName);
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String id = dbRef.child("documents").push().getKey();
                            if (id == null) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Failed to generate ID", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            HashMap<String, String> fileData = new HashMap<>();
                            fileData.put("id", id);
                            fileData.put("filename", fileName);
                            fileData.put("url", uri.toString());

                            dbRef.child("documents").child(id).setValue(fileData)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBar.setVisibility(View.GONE); // hide
                                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
                                        tvUploadStatus.setText("Uploaded: " + fileName);

                                        // Save last uploaded file ID for tagging
                                        lastUploadedFileId = id;
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE); // hide
                                        Toast.makeText(this, "Failed to save metadata", Toast.LENGTH_SHORT).show();
                                    });
                        }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE); // hide
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void addContact() {
        String name = etContactName.getText().toString().trim();
        String rel = etContactRel.getText().toString().trim();
        String phone = etContactPhone.getText().toString().trim();

        if (name.isEmpty() || rel.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all contact fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = dbRef.child("contacts").push().getKey();
        HashMap<String, String> contact = new HashMap<>();
        contact.put("id", id);
        contact.put("name", name);
        contact.put("relationship", rel);
        contact.put("phone", phone);
        // saving into database
        if (id != null) {
            dbRef.child("contacts").child(id).setValue(contact)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show());
        }
    }

    private void addLocation(LatLng latLng) {
        String id = dbRef.child("locations").push().getKey();

        HashMap<String, Object> location = new HashMap<>();
        location.put("id", id);
        location.put("lat", latLng.latitude);
        location.put("lon", latLng.longitude);
        location.put("name", "Safe Location");

        if (id != null) {
            dbRef.child("locations").child(id).setValue(location)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Location added", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show());
        }
    }

    private void showTagPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_tag, null);
        builder.setView(view);

        GridLayout tagContainer = view.findViewById(R.id.tagContainer);

        // Fixed tags with their colors
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
        builder.setPositiveButton("Save", (dialog, which) -> {
            saveTagsToFirebase(selectedTags);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveTagsToFirebase(Set<String> selectedTags) {
        if (lastUploadedFileId == null || lastUploadedFileId.isEmpty()) {
            Toast.makeText(this, "No uploaded file to tag", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tagsList = new ArrayList<>(selectedTags);

        dbRef.child("documents").child(lastUploadedFileId).child("tags").setValue(tagsList)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Tags saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save tags: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
