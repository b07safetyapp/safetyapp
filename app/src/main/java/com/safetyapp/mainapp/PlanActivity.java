package com.safetyapp.mainapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlanActivity extends BaseActivity {

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_plan;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check the login
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // get the items
        PlanAdapter adapter = new PlanAdapter(this.getApplicationContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setTitle("Planning");
    }
}
