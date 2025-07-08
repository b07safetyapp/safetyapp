package com.safetyapp.mainapp;

import java.util.ArrayList;
import java.util.List;

public class QuestionChoiceModel {
    public String id;
    public String label;
    public ArrayList<String> options; // if there are any
    public String choice;
    public QuestionChoiceModel(String id, String label, ArrayList<String> options){
        this.id = id;
        this.label = label;
        this.options = options;
    }

    public void setChoice(String choice){
        this.choice = choice;
    }

    public String getId(){ return this.id; }
    public String getLabel(){ return this.label; }
    public ArrayList<String> getOptions(){ return this.options; }
    public String getChoice(){ return this.choice; }
}
