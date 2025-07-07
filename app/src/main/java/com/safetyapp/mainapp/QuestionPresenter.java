package com.safetyapp.mainapp;

import android.content.Context;
import android.content.res.Resources;
import android.renderscript.ScriptGroup;
import android.util.Log;
import java.io.IOException;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class QuestionPresenter {
    private HashMap<String, QuestionModel> questions = new HashMap<>();
    private ArrayList<String> currentquestions = new ArrayList<>();
    private AppContext appcontext;

    public QuestionPresenter(){
        // get context
        this.appcontext = new AppContext();
        try{
            loadquestionsfromjson();
        }catch (Exception e){
            System.err.println(e);
        }
    }
    public void loadquestionsfromjson() throws IOException, JSONException{
        // open the resource file
        Context ctx = appcontext.getContext();
        InputStream IS;
        Resources resources = ctx.getResources();
        IS = resources.openRawResource(R.raw.questions2);
        // read the string contents and parse into json
        BufferedReader BR = new BufferedReader(new InputStreamReader(IS));
        StringBuilder SB = new StringBuilder();
        String line;
        while ((line = BR.readLine()) != null) {
            SB.append(line);
        }
        BR.close(); // prevent leaks
        JSONObject root = new JSONObject(SB.toString());
        JSONObject questionsObj = root.getJSONObject("items");
        // add the starting node to current questions
        currentquestions.add(root.getString("start"));
        // add all questions into the map
        Iterator<String> keys = questionsObj.keys();
        while(keys.hasNext()) {
            String id = keys.next();
            // create a new question object
            JSONObject currquestionjson = questionsObj.getJSONObject(id);
            String label = currquestionjson.getString("label");
            String type = currquestionjson.getString("type");
            ArrayList<String> options = new ArrayList<>();
            if (type.equals("multi")){
                JSONArray jArray = currquestionjson.getJSONArray("options");
                if (jArray != null) {
                    for (int i=0;i<jArray.length();i++){
                        options.add(jArray.getString(i));
                    }
                }
            }else{
                options = null;
            }
            // add next
            HashMap<String, String> next = new HashMap<>();
            if (!type.equals("end")) {
                JSONObject nextJson = currquestionjson.getJSONObject("next");
                // create hashmap from json object
                Iterator<String> nextkeys = nextJson.keys();
                while (nextkeys.hasNext()) {
                    String key = (String) keys.next();
                    String value = nextJson.getString(key);
                    next.put(key, value);
                }
            }
            else{
                next = null;
            }
            QuestionModel tempquestion = new QuestionModel(id,type,label,options, next);
            // add to the hashmap
            questions.put(id, tempquestion);

            // debugging log all items
            for (String key : questions.keySet()){
                Log.d("key: ", key);
                Log.d("> id:", questions.get(key).getId());
                Log.d("> type:", questions.get(key).getType());
                Log.d("> label:", questions.get(key).getLabel());
                Log.d("> options:", questions.get(key).getOptions().get(0));
                Log.d("> next:", questions.get(key).getNext().toString());
            }


        }
    }

}