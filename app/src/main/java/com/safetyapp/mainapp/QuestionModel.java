package com.safetyapp.mainapp;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
public class QuestionModel {
    private final String id;
    private final String type; // question type
    private final String label;
    @SuppressWarnings("FieldMayBeFinal") // can java shut up pls
    private List<String> options; // if there are any
    private HashMap<String, String> next; // if there are any

    public QuestionModel(String id, String type, String label, List<String> options, HashMap<String, String> next) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.options = options;
        this.next = next;
    }

    public String getId(){ return id; }
    public String getType(){ return type; }
    public String getLabel(){ return label; }
    public List<String> getOptions(){ return options; }
    public HashMap<String, String> getNext(){ return next; }

}
