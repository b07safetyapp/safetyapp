package com.safetyapp.mainapp;

import android.content.Intent;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


public abstract class BaseActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);


        FrameLayout placeholder = findViewById(R.id.base_content);
        getLayoutInflater().inflate(getContentLayoutId(), placeholder, true);


        // exit button
        Button exitButton = findViewById(R.id.exitbtn);
        // exit button event listen
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse("https://www.youtube.com/");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);

                startActivity(webIntent);

                finishAffinity();
                finishAndRemoveTask();
            }
        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Button 1 - Settings Activity
        Button button1 = findViewById(R.id.settings);
        button1.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class); // make this later
            startActivity(intent);
        });

        // Button 2 - Logout
        Button button2 = findViewById(R.id.logout);
        button2.setOnClickListener(v -> logoutUser());
    }

    protected abstract @LayoutRes int getContentLayoutId();

    private void logoutUser() {
        if (mAuth != null) {
            mAuth.signOut();
            // Redirect to login activity or main activity after logout
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (this != null) {
                this.finish();
            }
        }
    }
}

