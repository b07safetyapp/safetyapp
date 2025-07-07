package com.safetyapp.mainapp;

import java.util.List;

public class QuestionChoiceModel {
    public String id;
    public String label;
    public List<String> options; // if there are any
    public String choice;
    public QuestionChoiceModel(String id, String label, List<String> options){
        this.id = id;
        this.label = label;
        this.options = options;
    }

    public void setChoice(String choice){
        this.choice = choice;
    }

    public String getId(){ return this.id; }
    public String getLabel(){ return this.label; }
    public List<String> getOptions(){ return this.options; }
    public String getChoice(){ return this.choice; }
}
