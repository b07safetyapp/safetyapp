package com.safetyapp.mainapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Default Page");
        // check the login


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