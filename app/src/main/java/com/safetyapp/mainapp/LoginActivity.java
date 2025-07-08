package com.safetyapp.mainapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {
    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String savedPin = new PINManager().getSavedPin(this);
            if (savedPin != null) {
                showPinLoginDialog();
            } else {
                promptUserToSetPin();
                updateUI(currentUser);
            }
        } else {
            showLoginScreen();
        }
    }

    private void showLoginScreen() {
        setContentView(R.layout.activity_login); // ensure you have activity_login.xml

        EditText emailField = findViewById(R.id.email_field);
        EditText passwordField = findViewById(R.id.password_field);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String savedPin = new PINManager().getSavedPin(this);
                            if (savedPin == null) {
                                promptUserToSetPin();
                            }
                            updateUI(user);
                        } else {
                            Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void promptUserToSetPin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set a 6-digit PIN for future logins");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String pin = input.getText().toString();
            if (pin.length() == 6) {
                new PINManager().savePin(LoginActivity.this, pin);
                Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "PIN must be 6 digits", Toast.LENGTH_SHORT).show();
            }
        });

        //builder.setNegativeButton("Skip", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showPinLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your 6-digit PIN");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        builder.setView(input);

        builder.setPositiveButton("Login", (dialog, which) -> {
            String enteredPin = input.getText().toString();
            String savedPin = new PINManager().getSavedPin(LoginActivity.this);
            if (savedPin != null && savedPin.equals(enteredPin)) {
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void reload() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateUI(user);
                } else {
                    Toast.makeText(this, "Failed to reload user.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}