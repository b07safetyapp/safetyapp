package com.safetyapp.mainapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;


public class SupportConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_connection);



        // Temp, will change later
        String userCity = "Toronto";




        SupportDataParser parser = new SupportDataParser(this);
        HashMap<String, List<SupportResource>> allData = parser.parse();


        // If no data for the city, use an empty list.
        List<SupportResource> citySpecificList = allData.get(userCity);
        if (citySpecificList == null) {
            citySpecificList = new ArrayList<>();
        }



        RecyclerView recyclerView = findViewById(R.id.supportRecyclerView);

        SupportResourceAdapter adapter = new SupportResourceAdapter(citySpecificList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}