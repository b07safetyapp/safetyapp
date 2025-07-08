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
    private static HashMap<String, QuestionModel> questions = new HashMap<>();
    public static ArrayList<String> currentquestions = new ArrayList<>();
    public static ArrayList<String> currentchoices = new ArrayList<>();
    private AppContext appcontext;
    private JSONObject root;

    public QuestionPresenter(){
        // get context
        this.appcontext = new AppContext();
        try{
            loadquestionsfromjson();
        }catch (Exception e){
            System.err.println(e);
        }
    }

    private QuestionModel parsequestion(String id) throws IOException, JSONException{
        JSONObject questionsObj = this.root.getJSONObject("items");
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
                String nextkey = (String) nextkeys.next();
                String nextvalue = nextJson.getString(nextkey);
                next.put(nextkey, nextvalue);
            }
        }
        else{
            next = null;
        }
        return new QuestionModel(id,type,label,options, next);
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
        root = new JSONObject(SB.toString());
        JSONObject questionsObj = this.root.getJSONObject("items");
        // add the starting node to current questions
        this.addquestion(this.root.getString("start"));
        // add all questions into the map
        Iterator<String> keys = questionsObj.keys();
        while(keys.hasNext()) {
            String id = keys.next();
            questions.put(id, parsequestion(id));
        }
    }

    public static QuestionChoiceModel getcurrentquestion(){
        String currentid = currentquestions.get(currentquestions.size() - 2);
        String currentchoice = currentchoices.get(currentchoices.size() - 2);
        QuestionChoiceModel retchoicemodel = new QuestionChoiceModel(currentid, questions.get(currentid).getLabel(), questions.get(currentid).getOptions());
        retchoicemodel.setChoice(currentchoice);
        // set the return to the current last question
        return retchoicemodel;
    }

    public void addquestion(String id){
        currentquestions.add(id);
        currentchoices.add("tempchoice");
    }

    public void changechoice(String id, String choice){
        //find the id
        int index = currentquestions.indexOf(id);
        // remove all that is greater than id
        for (int i = currentquestions.size()-1; i > index; i--){
            currentchoices.remove(i);
            currentquestions.remove(i);
        }
        this.currentchoices.set(index, choice);
    }

}