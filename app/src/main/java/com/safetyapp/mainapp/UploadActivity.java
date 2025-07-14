package com.safetyapp.mainapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    // File Upload UI
    @SuppressWarnings("FieldCanBeLocal")
    private CardView cardUpload;
    private TextView tvUploadStatus;
    @SuppressWarnings("FieldCanBeLocal")
    private Button btnAddTags, btnViewDocs;
    private ProgressBar progressBar;

    // Contact Fields
    private EditText etContactName, etContactRel, etContactPhone;
    @SuppressWarnings("FieldCanBeLocal")
    private Button btnAddContact, btnEditContacts;

    // Location
    @SuppressWarnings("FieldCanBeLocal")
    private Button btnAddLoc, btnEditLocs;
    private LatLng selectedLatLng;
    private GoogleMap mMap;
    private SearchView mapSearchView;

    // Firebase
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Firebase
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Upload Section
        cardUpload = findViewById(R.id.cardUpload);
        tvUploadStatus = findViewById(R.id.tvUploadStatus);
        btnAddTags = findViewById(R.id.btnViewDocs);
        btnViewDocs = findViewById(R.id.btnEditDocs);
        progressBar = findViewById(R.id.progressBarUpload);

        cardUpload.setOnClickListener(v -> chooseFile());

        btnAddTags.setOnClickListener(v -> {
            Toast.makeText(this, "Add Tags screen coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, TagActivity.class)); // TODO
        });

        btnViewDocs.setOnClickListener(v ->
                startActivity(new Intent(this, FileviewActivity.class))
        );

        // Contacts
        etContactName = findViewById(R.id.etContactName);
        etContactRel = findViewById(R.id.etContactRel);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnEditContacts = findViewById(R.id.btnEditContacts);

        btnAddContact.setOnClickListener(v -> addContact());
        btnEditContacts.setOnClickListener(v ->
                startActivity(new Intent(this, ContactActivity.class))
        );

        // Locations
        btnAddLoc = findViewById(R.id.btnViewLocs);
        btnEditLocs = findViewById(R.id.btnEditLocs);

        btnAddLoc.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Select a location on the map", Toast.LENGTH_SHORT).show();
                return;
            }
            addLocation(selectedLatLng);
        });

        btnEditLocs.setOnClickListener(v ->
                Toast.makeText(this, "Location List screen coming soon", Toast.LENGTH_SHORT).show()
                //startActivity(new Intent(this, LocationListActivity.class)) // TODO
        );

        // Setup Map
        mapSearchView = new SearchView(this);
        FrameLayout mapFrame = findViewById(R.id.mapFragment);
        mapFrame.addView(mapSearchView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;

                mMap.setOnMapClickListener(latLng -> {
                    selectedLatLng = latLng;
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Safe Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                });

                setupSearchView();
            });
        }
    }

    private void setupSearchView() {
        mapSearchView.setQueryHint("Search location");
        mapSearchView.setIconifiedByDefault(false);
        mapSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private void searchLocation(String query) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(query, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                selectedLatLng = latLng;

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(query));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show();
        }
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
                            HashMap<String, String> fileData = new HashMap<>();
                            fileData.put("id", id);
                            fileData.put("filename", fileName);
                            fileData.put("url", uri.toString());

                            assert id != null;
                            dbRef.child("documents").child(id).setValue(fileData)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBar.setVisibility(View.GONE); // hide
                                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
                                        tvUploadStatus.setText("Uploaded: " + fileName);
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

        assert id != null;
        dbRef.child("contacts").child(id).setValue(contact)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show());
    }

    private void addLocation(LatLng latLng) {
        String id = dbRef.child("locations").push().getKey();

        HashMap<String, Object> location = new HashMap<>();
        location.put("id", id);
        location.put("lat", latLng.latitude);
        location.put("lon", latLng.longitude);
        location.put("name", "Safe Location");

        assert id != null;
        dbRef.child("locations").child(id).setValue(location)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Location added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show());
    }
}
