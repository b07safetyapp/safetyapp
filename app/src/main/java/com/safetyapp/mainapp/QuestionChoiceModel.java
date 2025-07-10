package com.safetyapp.mainapp;

import java.util.ArrayList;
import java.util.List;

public class QuestionChoiceModel {
    public String id;
    public String label;
    public String type;
    public ArrayList<String> options; // if there are any
    public String choice;
    public String response;
    public QuestionChoiceModel(String id, String label, String type, ArrayList<String> options){
        this.id = id;
        this.label = label;
        this.type = type;
        this.options = options;
    }

    public void setChoice(String choice){
        this.choice = choice;
    }
    public void setResponse(String response){
        this.response = response;
    }

    public String getId(){ return this.id; }
    public String getType(){ return this.type; }
    public String getLabel(){ return this.label; }
    public ArrayList<String> getOptions(){ return this.options; }
    public String getChoice(){ return this.choice; }
}
