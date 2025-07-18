package com.safetyapp.mainapp;

import android.content.Intent;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        Button exitButton = findViewById(R.id.button2);
//
//        exitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri webpage = Uri.parse("https://www.youtube.com/");
//                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
//
//                startActivity(webIntent);
//
//                finishAffinity();
//            }
//        });

        setTitle("Default Page");
        // check the login
        QuestionPresenter mypresenter = new QuestionPresenter();


    }
    public void gousersetup(View V){
       Log.d("going", "going to user setup");
    }
    public void gohomepage(View V){
        Log.d("going", "going to home page");
        // route to homepage
        Intent i = new Intent(this, HomeActivity.class);
        i.putExtra("mykey", "myvalue");
        startActivity(i);
    }
}