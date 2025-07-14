package com.safetyapp.mainapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private ImageView ivUploadIcon;
    private TextView tvStatus;
    private Button btnChooseFile, btnUpload, btnAddTags, btnViewDocs;
    private EditText etContactName, etContactRel, etContactPhone;
    private Button btnAddContact, btnViewContacts;
    private Button btnAddLocation, btnViewLocations;

    private Uri fileUri;
    private StorageReference storageRef;
    private DatabaseReference dbRef;
    private ProgressDialog progressDialog;

    // Google Maps
    private GoogleMap mMap;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // FILE UPLOAD
        ivUploadIcon = findViewById(R.id.ivUploadIcon);
        tvStatus = findViewById(R.id.tvStatus);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnUpload = findViewById(R.id.btnUpload);
        btnAddTags = findViewById(R.id.btnAddTags);
        btnViewDocs = findViewById(R.id.btnViewDocs);

        // CONTACT FIELDS
        etContactName = findViewById(R.id.etContactName);
        etContactRel = findViewById(R.id.etContactRel);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnViewContacts = findViewById(R.id.btnEditContacts);

        // LOCATION BUTTONS
        btnAddLocation = findViewById(R.id.btnViewLocs);
        btnViewLocations = findViewById(R.id.btnEditLocs);

        // Firebase Refs
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        dbRef = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        // Choose File
        btnChooseFile.setOnClickListener(v -> openFileChooser());

        // Upload File
        btnUpload.setOnClickListener(v -> {
            if (fileUri != null) {
                uploadFile();
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Tags - (to be implemented)
        btnAddTags.setOnClickListener(v -> {
            // TODO: Launch AddTagsActivity when implemented
            Toast.makeText(this, "Add Tags screen coming soon", Toast.LENGTH_SHORT).show();
        });

        // View Documents
        btnViewDocs.setOnClickListener(v -> {
            Intent intent = new Intent(UploadActivity.this, FileviewActivity.class);
            startActivity(intent);
        });

        // Add Contact
        btnAddContact.setOnClickListener(v -> addEmergencyContact());

        // View All Contacts
        btnViewContacts.setOnClickListener(v -> {
            Intent intent = new Intent(UploadActivity.this, ContactActivity.class);
            startActivity(intent);
        });

        // View Locations
        btnViewLocations.setOnClickListener(v -> {
            Intent intent = new Intent(UploadActivity.this, LocationListActivity.class);
            startActivity(intent);
        });

        // Add Location
        btnAddLocation.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Please tap a location on the map", Toast.LENGTH_SHORT).show();
                return;
            }
            addSafeLocation(selectedLatLng);
        });

        // Initialize Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;
            mMap.setOnMapClickListener(latLng -> {
                selectedLatLng = latLng;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
            });
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            tvStatus.setText("Selected: " + fileUri.getLastPathSegment());
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    private void uploadFile() {
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String fileName = System.currentTimeMillis() + "." + getFileExtension(fileUri);
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String uploadId = dbRef.child("documents").push().getKey();
                            HashMap<String, String> fileData = new HashMap<>();
                            fileData.put("id", uploadId);
                            fileData.put("filename", fileName);
                            fileData.put("url", uri.toString());

                            dbRef.child("documents").child(uploadId).setValue(fileData)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
                                        tvStatus.setText("Uploaded: " + fileName);
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Failed to store metadata", Toast.LENGTH_SHORT).show();
                                    });
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void addEmergencyContact() {
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

        dbRef.child("contacts").child(id).setValue(contact)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
                );
    }

    private void addSafeLocation(LatLng latLng) {
        String id = dbRef.child("locations").push().getKey();

        HashMap<String, Object> location = new HashMap<>();
        location.put("id", id);
        location.put("lat", latLng.latitude);
        location.put("lon", latLng.longitude);
        location.put("name", "Safe Location");

        dbRef.child("locations").child(id).setValue(location)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Location added", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add location", Toast.LENGTH_SHORT).show()
                );
    }
}
