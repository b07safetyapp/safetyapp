package com.safetyapp.mainapp;

import android.content.Intent;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button1);
        EditText editText1 = findViewById(R.id.editText1);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String text = editText1.getText().toString();

                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();

            }
        });

        Button exitButton = findViewById(R.id.button2);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse("https://www.youtube.com/");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);

                startActivity(webIntent);

                finishAffinity();
            }
        });

        setTitle("Default Page");
        // check the login

        // route to login
        Intent i = new Intent(this, HomeActivity.class);
        // send data to next activity
        i.putExtra("mykey", "myvalue");
        // route to next activity
        startActivity(i);

    }
}