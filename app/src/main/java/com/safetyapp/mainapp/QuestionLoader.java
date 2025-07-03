package com.safetyapp.mainapp;

import android.content.Context;
import org.json.*;
import java.util.*;
import java.io.*;
public class QuestionLoader {
    private static Map<Integer, JSONObject> loadFromJSON(Context ctx) throws IOException, JSONException {
        /*
         * Load the raw JSON definitions from raw/questions.json
         * @param ctx the Android context, needed to open the asset
         * @return a Map from question ID → its raw JSONObject definition
         * @throws IOException   if the file can’t be read
         * @throws JSONException if parsing fails
         */
        InputStream IS = ctx.getAssets().open("res/raw/questions.json");
        BufferedReader BR = new BufferedReader(new InputStreamReader(IS));
        StringBuilder SB = new StringBuilder();
        String line;
        while ((line = BR.readLine()) != null) {
            SB.append(line);
        }
        BR.close(); // prevent leaks
        JSONObject root = new JSONObject(SB.toString());
        JSONObject questionsObj = root.getJSONObject("questions");
        return toMap(questionsObj);
    }

    private static Map<Integer, JSONObject> toMap(JSONObject obj) throws JSONException{
        Map<Integer, JSONObject> map = new HashMap<>();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject content = obj.getJSONObject(key);
            int id;
            try{
                id = Integer.parseInt(key);
            }
            catch (NumberFormatException E){
                continue;
            }
            map.put(id, content);
        }
        return map;
    }

    public static Map<Integer, Question> loadQuestions(Context ctx) throws IOException, JSONException{
        Map<Integer, JSONObject> stuff = loadFromJSON(ctx);
        Map<Integer, Question> map = new HashMap<>();
        for (Map.Entry<Integer, JSONObject> entry : stuff.entrySet()){
            JSONObject v = entry.getValue();
            String type = v.getString("type");
            String label = v.getString("value");
            int id = entry.getKey();
            List<String> options = null;
            if (v.has("options")){
                JSONArray A = v.getJSONArray("options");
                options = new ArrayList<>();
                for (int i=0; i<A.length(); i++){
                    options.add(A.getString(i));
                }
            }
            Question Q = new Question(id, type, label, options);
            map.put(id, Q);
        }
        return map;
    }
}