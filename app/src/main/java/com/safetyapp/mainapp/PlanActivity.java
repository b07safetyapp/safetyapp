package com.safetyapp.mainapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class PlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        setTitle("Planning");
        // check the login
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // get the items
        PlanAdapter adapter = new PlanAdapter();
        recyclerView.setAdapter(adapter);
    }
}
