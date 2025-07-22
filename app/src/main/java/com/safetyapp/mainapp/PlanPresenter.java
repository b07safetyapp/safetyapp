package com.safetyapp.mainapp;

import android.content.Context;
import android.content.res.Resources;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
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

    private static ArrayList<String> tipsarr = new ArrayList<>();
    private static ArrayList<String> questions = new ArrayList<>();
    private static ArrayList<String> choices = new ArrayList<>();
    public static ArrayList<Boolean> currentchecks = new ArrayList<>();
    private static AppContext appcontext;
    private static Context ctx;
    private JSONObject root;

    public PlanPresenter() {
        // get context
        this.appcontext = new AppContext();
        this.ctx = appcontext.getContext();
        try {
            readquestionchoicesfromjson();
            loadplansfromjson();
        } catch (Exception e) {
            System.err.println(e);
        }
        // add the default question to the questions list and choices list
    }


    public void loadplansfromjson() throws IOException, JSONException {
        // purge everything
        currentchecks.clear();
        tipsarr.clear();
        Log.d("hello everynan", "");
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
            // get the list of tips
            JSONArray tipjson = planObj.getJSONArray("Tips");
            ArrayList<String> tips = new ArrayList<>();
            for (int i = 0; i < tipjson.length(); i++) {
                tips.add(tipjson.getString(i));
            }
            // check if all conditions are met before adding tips
            boolean all_satisfied = true;
            for (Iterator<String> i = conditions.keys(); i.hasNext(); ) {
                String keyy = i.next();
                JSONArray conditionslist = conditions.getJSONArray(keyy);
                ArrayList<String> conditionsreq = new ArrayList<>();
                for (int c = 0; c < conditionslist.length(); c++) {
                    conditionsreq.add(conditionslist.getString(c));
                }
                // locate the key within questions
                int keyindex = questions.indexOf(keyy);
                if (keyindex == -1) {
                    all_satisfied = false;
                    continue;
                }
                if (!conditionsreq.contains(choices.get(keyindex))) {
                    all_satisfied = false;
                }
            }
            if (all_satisfied == true){
                for (int i = 0 ; i < tips.toArray().length; i++){
                    tipsarr.add(tips.get(i));
                    currentchecks.add(false);
                }
            }
            // create a json map from the current json
        }
        for (int i = 0; i < tipsarr.toArray().length; i++){
            Log.d("current tips[i] :", tipsarr.get(i));
        }
    }

    public void readquestionchoicesfromjson() throws IOException, JSONException{
        // open the json file
        File myfile = new File(ctx.getFilesDir(), "questionresults.json");
        // parse the json file into a json object
        InputStream inputStream = new FileInputStream(myfile);
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        String data = resultStringBuilder.toString();
        // parse this json and get each corresponding key
        JSONObject root = new JSONObject(data);
        // create a list of the keys and values
        Iterator<String> rootkeys = root.keys();
        // populate each list
        while(rootkeys.hasNext()){
            String currkey = rootkeys.next();
            questions.add(currkey);
            choices.add(root.getString(currkey));
        }
    }

    public ArrayList<String> getTipsarr(){
        return this.tipsarr;
    }

    public ArrayList<Boolean> getChecks(){
        return this.currentchecks;
    }
}