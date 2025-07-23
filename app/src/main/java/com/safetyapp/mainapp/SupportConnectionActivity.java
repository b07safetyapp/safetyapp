package com.safetyapp.mainapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class SupportConnectionActivity extends BaseActivity {
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_support_connection;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Temp, will change later
        String userCity = "Toronto";
        // read the current city from the saved questionaire json
        // open the file and parse into json
        try{
            File questionresultsjson = new File(this.getFilesDir(), "questionresults.json");
            String outputresultjsoncontents = "";
            Scanner fileScanner = new Scanner(questionresultsjson);
            while (fileScanner.hasNext()){
                outputresultjsoncontents += fileScanner.nextLine();
            }
            fileScanner.close();
            // parse into a json file
            JSONObject rootjson = new JSONObject(outputresultjsoncontents);
            userCity = rootjson.getString("q0");
        }
        catch (Exception e){
            Log.d("error", e.getMessage());
        }
        Log.d("USER CITY", userCity);


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