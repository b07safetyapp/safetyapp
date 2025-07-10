package com.safetyapp.mainapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private CheckBox disclaimerCheck;
    private Button signupButton;
    private FirebaseAuth mAuth;

    private static final String PREFS_FILENAME = "secure_prefs";
    private static final String PIN_KEY = "user_pin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);  // replace with your layout XML

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        disclaimerCheck = findViewById(R.id.disclaimerCheck);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailInput.setError("Email is required");
                    emailInput.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordInput.setError("Password is required");
                    passwordInput.requestFocus();
                    return;
                }

                if (!isValidPassword(password)) {
                    passwordInput.setError("Password must contain at least one uppercase, lowercase, digit, and special character");
                    passwordInput.requestFocus();
                    return;
                }

                if (!disclaimerCheck.isChecked()) {
                    Toast.makeText(SignupActivity.this, "Accepting the disclaimer is required to continue", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user in Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();

                                    // Prompt for PIN creation
                                    showPinDialog();
                                } else {
                                    Toast.makeText(SignupActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private boolean isValidPassword(String password) {
        Pattern digit = Pattern.compile(".*\\d.*");
        Pattern lowercase = Pattern.compile(".*[a-z].*");
        Pattern uppercase = Pattern.compile(".*[A-Z].*");
        Pattern special = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return digit.matcher(password).matches()
                && lowercase.matcher(password).matches()
                && uppercase.matcher(password).matches()
                && special.matcher(password).matches();
    }

    private void showPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create a 6-digit PIN");

        // Create layout with two EditTexts (PIN and confirm PIN)
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText pinInput = new EditText(this);
        pinInput.setHint("Enter PIN");
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        layout.addView(pinInput);

        final EditText pinConfirmInput = new EditText(this);
        pinConfirmInput.setHint("Confirm PIN");
        pinConfirmInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinConfirmInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        layout.addView(pinConfirmInput);

        builder.setView(layout);

        builder.setPositiveButton("Save PIN", null);  // We'll override later to prevent auto-dismiss
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(SignupActivity.this, "PIN setup canceled", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();

        // Override the positive button so we can validate input first
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String pin = pinInput.getText().toString().trim();
                String confirmPin = pinConfirmInput.getText().toString().trim();

                if (pin.length() != 6) {
                    pinInput.setError("PIN must be exactly 6 digits");
                    pinInput.requestFocus();
                    return;
                }
                if (!pin.matches("\\d{6}")) {
                    pinInput.setError("PIN must contain only digits");
                    pinInput.requestFocus();
                    return;
                }
                if (!pin.equals(confirmPin)) {
                    pinConfirmInput.setError("PINs do not match");
                    pinConfirmInput.requestFocus();
                    return;
                }

                try {
                    savePinSecurely(pin);
                    Toast.makeText(SignupActivity.this, "PIN saved securely", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SignupActivity.this, "Failed to save PIN", Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void savePinSecurely(String pin) throws GeneralSecurityException, IOException {
        // Create or retrieve the Master Key for encryption/decryption
        MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // Create EncryptedSharedPreferences instance
        androidx.security.crypto.EncryptedSharedPreferences encryptedSharedPreferences =
                (androidx.security.crypto.EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                        this,
                        PREFS_FILENAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

        // Store the PIN securely
        encryptedSharedPreferences.edit()
                .putString(PIN_KEY, pin)
                .apply();
    }
}
