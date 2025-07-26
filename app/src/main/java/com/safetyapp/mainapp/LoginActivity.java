package com.safetyapp.mainapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private EditText emailInput, passwordInput, pinInput;
    @SuppressWarnings("FieldCanBeLocal")
    private Button loginEmailButton, loginPinButton;

    private FirebaseAuth mAuth;

    // Constants
    private static final String PREFS_FILENAME = "secure_prefs";
    private static final String PIN_KEY = "user_pin";
    private static final String PIN_ATTEMPTS_KEY = "pin_attempts";
    private static final String LOCKOUT_TIME_KEY = "lockout_timestamp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Find views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        pinInput = findViewById(R.id.pinInput);
        loginEmailButton = findViewById(R.id.loginEmailButton);
        loginPinButton = findViewById(R.id.loginPinButton);

        // Mask inputs after 2s delay
        //setupDelayedMasking(passwordInput);
        //setupDelayedMasking(pinInput);

        // Set button actions
        loginEmailButton.setOnClickListener(v -> loginWithEmail());
        loginPinButton.setOnClickListener(v -> loginWithPin());
    }

    // 🔐 Email + password login via Firebase
    private void loginWithEmail() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        Toast.makeText(this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // 🔢 PIN login with progressive lockout
    private void loginWithPin() {
        String inputPin = pinInput.getText().toString().trim();

        if (inputPin.length() != 6 || !inputPin.matches("\\d{6}")) {
            Toast.makeText(this, "Enter a valid 6-digit PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("attempts", MODE_PRIVATE);
        int attempts = prefs.getInt(PIN_ATTEMPTS_KEY, 0);
        long now = System.currentTimeMillis();
        long lockoutUntil = prefs.getLong(LOCKOUT_TIME_KEY, 0);

        // 🚫 Check if currently locked
        if (now < lockoutUntil) {
            long remaining = (lockoutUntil - now) / 1000;
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            String msg = String.format("Account locked. Try again in %d:%02d", minutes, seconds);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }

        String savedPin = getSavedPin();
        if (savedPin == null) {
            Toast.makeText(this, "No PIN found. Please sign up first.", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ Successful PIN login
        if (inputPin.equals(savedPin)) {
            resetPinAttempts();
            Toast.makeText(this, "PIN login successful", Toast.LENGTH_SHORT).show();
            goToHome();
        } else {
            // ❌ Wrong PIN: increment and apply lockout
            incrementPinAttempts();

            int updatedAttempts = prefs.getInt(PIN_ATTEMPTS_KEY, 0);
            long lockDuration = getLockoutDurationMillis(updatedAttempts);
            if (lockDuration > 0) {
                long lockUntil = System.currentTimeMillis() + lockDuration;
                prefs.edit().putLong(LOCKOUT_TIME_KEY, lockUntil).apply();
            }

            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔐 Get securely stored PIN
    private String getSavedPin() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                    this,
                    PREFS_FILENAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return encryptedPrefs.getString(PIN_KEY, null);

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ➕ Increase attempt counter
    private void incrementPinAttempts() {
        SharedPreferences prefs = getSharedPreferences("attempts", MODE_PRIVATE);
        int current = prefs.getInt(PIN_ATTEMPTS_KEY, 0);
        prefs.edit().putInt(PIN_ATTEMPTS_KEY, current + 1).apply();
    }

    // 🔁 Reset attempts and unlock
    private void resetPinAttempts() {
        SharedPreferences prefs = getSharedPreferences("attempts", MODE_PRIVATE);
        prefs.edit()
                .putInt(PIN_ATTEMPTS_KEY, 0)
                .remove(LOCKOUT_TIME_KEY)
                .apply();
    }

    // ⏱ Get lockout duration based on how many failed attempts
    private long getLockoutDurationMillis(int attempts) {
        if (attempts == 4) return 2 * 60 * 1000;     // 2 min
        if (attempts == 6) return 5 * 60 * 1000;     // 5 min
        if (attempts == 8) return 10 * 60 * 1000;   // 10 min
        if (attempts == 10) return 60 * 60 * 1000;   // 60 min
        if (attempts >= 13) return Integer.MAX_VALUE; // 24 days
        return 0;
    }

    // ➡️ Redirect to home screen
    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ⏳ Mask password/PIN 2 seconds after user stops typing
    private void setupDelayedMasking(EditText editText) {
        Handler handler = new Handler();
        Runnable maskRunnable = () ->
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setTransformationMethod(null);  // Show while typing

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setTransformationMethod(null); // Unmask
                handler.removeCallbacks(maskRunnable);  // Reset delay
                handler.postDelayed(maskRunnable, 2000); // Mask after 2s
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
