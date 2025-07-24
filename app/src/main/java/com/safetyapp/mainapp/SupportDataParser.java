package com.safetyapp.mainapp;

import android.content.Context;
import org.json.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class SupportDataParser {

    private final Context context;

    public SupportDataParser(Context context) {
        this.context = context;
    }

    /**
     * Parse JSON and return the data.
     * @return A HashMap of city->resourcesList.
     */
    public HashMap<String, List<SupportResource>> parse() {
        HashMap<String, List<SupportResource>> supportMap = new HashMap<>();
        String jsonString;

        //Read JSON file
        try {
            InputStream is = context.getAssets().open("support_connections.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return supportMap; // Return empty map if file loading fails
        }



        try {
            JSONObject rootObject = new JSONObject(jsonString);
            Iterator<String> keys = rootObject.keys();

            while (keys.hasNext()) {
                String city = keys.next();
                JSONArray servicesArray = rootObject.getJSONArray(city);
                List<SupportResource> resourcesList = new ArrayList<>();

                for (int i = 0; i < servicesArray.length(); i++) {
                    JSONObject serviceObject = servicesArray.getJSONObject(i);
                    String name = serviceObject.getString("name");
                    String category = serviceObject.getString("category");
                    String contact = serviceObject.getString("contact");
                    String website = serviceObject.getString("website");


                    // Parse location array if it exists and is not null
                    List<Double> location = null;
                    if (serviceObject.has("location") && !serviceObject.isNull("location")) {
                        JSONArray locationArray = serviceObject.getJSONArray("location");
                        if (locationArray.length() == 2) {
                            location = new ArrayList<>();
                            location.add(locationArray.getDouble(0)); // Latitude
                            location.add(locationArray.getDouble(1)); // Longitude
                        }
                    }

                    resourcesList.add(new SupportResource(name, category, contact, website, location));
                }
                supportMap.put(city, resourcesList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return supportMap;
    }

}