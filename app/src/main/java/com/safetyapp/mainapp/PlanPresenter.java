package com.safetyapp.mainapp;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlanPresenter {

    // parsing the json

    private static ArrayList<String> tips = new ArrayList<>();
    private static ArrayList<String> questions;
    private static ArrayList<String> choices;
    public static ArrayList<String> currentchecks = new ArrayList<>();
    private static AppContext appcontext;
    private static Context ctx;
    private JSONObject root;

    public PlanPresenter() {
        // get context
        this.appcontext = new AppContext();
        try {
            loadplansfromjson();
        } catch (Exception e) {
            System.err.println(e);
        }
        // add the default question to the questions list and choices list
    }


    public void loadplansfromjson() throws IOException, JSONException {
        // purge everything (for testing)
        currentchecks.clear();
        // open the resource file
        ctx = appcontext.getContext();
        InputStream IS;

        Resources resources = ctx.getResources();
        IS = resources.openRawResource(R.raw.plans);
        // read the string contents and parse into json
        BufferedReader BR = new BufferedReader(new InputStreamReader(IS));
        StringBuilder SB = new StringBuilder();
        String line;
        while ((line = BR.readLine()) != null) {
            SB.append(line);
        }
        BR.close(); // prevent leaks

        JSONObject root = new JSONObject(SB.toString());
        // convert into map
        for (Iterator<String> it = root.keys(); it.hasNext(); ) {
            String key = it.next();
            // get the current json from this key
            JSONObject planObj = root.getJSONObject(key);
            JSONObject conditions = planObj.getJSONObject("Criteria");
            JSONArray tips = conditions.getJSONArray("Tips");
            List<String> tipsarr = new ArrayList<String>();
            for (Iterator<String> i = root.keys(); i.hasNext(); ) {
                String keyy = i.next();
                JSONArray conditionlist = conditions.getJSONArray(keyy);
                ArrayList<String> conditionarr = new ArrayList<String>();
                for (int v = 0; v < conditionlist.length(); v++) {
                    conditionarr.add(conditionlist.getJSONObject(v).getString("name"));
                }
                // locate the key within questions
                int keyindex = conditionarr.indexOf(keyy);
                if (keyindex == -1) {
                    continue;
                }
                if (conditionarr.contains(choices.get(keyindex))) {
                    for (int q = 0; q < tipsarr.toArray().length; q++) {
                        tipsarr.add(tipsarr.get(q));
                    }
                }
            }
            // create a json map from the current json
        }
    }
}