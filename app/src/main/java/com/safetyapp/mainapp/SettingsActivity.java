package com.safetyapp.mainapp;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.regex.Pattern;

public class SettingsActivity extends BaseActivity {

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_settings;
    }
    private EditText oldPinEditText, newPinEditText, confirmPinEditText;
    private EditText oldPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private SwitchMaterial notificationsSwitch;
    private Button deleteAccountButton;

    private SharedPreferences encryptedPrefs;
    private static final String PIN_KEY = "user_pin";
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[!@#$%^&*]).{6,}$");

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize EncryptedSharedPreferences
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    this,
                    "secret_shared_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize secure storage", Toast.LENGTH_LONG).show();
        }

        // Initialize views
        oldPinEditText = findViewById(R.id.oldPinEditText);
        newPinEditText = findViewById(R.id.newPinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Notifications
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Notifications turned on" : "Notifications turned off";
            Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
        });

        // Delete account
        deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());

        // Add listeners for PIN/password changes
        findViewById(R.id.changePinButton).setOnClickListener(v -> changePin());
        findViewById(R.id.changePasswordButton).setOnClickListener(v -> changePassword());
    }

    private void changePin() {
        String oldPin = oldPinEditText.getText().toString().trim();
        String newPin = newPinEditText.getText().toString().trim();
        String confirmPin = confirmPinEditText.getText().toString().trim();

        String savedPin = encryptedPrefs.getString(PIN_KEY, "");

        if (!Objects.equals(oldPin, savedPin)) {
            Toast.makeText(this, "Old PIN is incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPin.matches("\\d{6}")) {
            Toast.makeText(this, "New PIN must be exactly 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPin.equals(confirmPin)) {
            Toast.makeText(this, "New PIN and confirm PIN do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        encryptedPrefs.edit().putString(PIN_KEY, newPin).apply();
        Toast.makeText(this, "PIN updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void changePassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            Toast.makeText(this, "Password must contain at least one number and one special character", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New password and confirmation do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        setTitle("Settings Page");

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPassword))
                .addOnSuccessListener(authResult -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show());
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This action is irreversible. Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // 1. Delete from Realtime Database
        DatabaseReference rtdbRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        rtdbRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // 2. Delete from Firebase Storage
                    FirebaseStorage.getInstance().getReference("users/" + uid).listAll()
                            .addOnSuccessListener(listResult -> {
                                // Delete all files in user's folder
                                for (StorageReference fileRef : listResult.getItems()) {
                                    fileRef.delete();
                                }
                                // Optionally delete folders too (listResult.getPrefixes())

                                // 3. Finally, delete Firebase Auth user
                                user.delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(this, "Account and data deleted", Toast.LENGTH_SHORT).show();
                                            finish(); // or redirect to login
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Auth deletion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Storage deletion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Realtime DB deletion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    // Stub for location change handling
    private void changeLocation() {
        // Do nothing for now
    }
}
