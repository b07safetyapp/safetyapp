package com.safetyapp.mainapp;

import android.content.Intent;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;


public abstract class BaseActivity extends AppCompatActivity {
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
    }


    protected abstract @LayoutRes int getContentLayoutId();
}

