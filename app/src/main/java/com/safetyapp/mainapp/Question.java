package com.safetyapp.mainapp;

import java.util.List;
public class Question {
    private final int identifier;
    private final String type; // question type
    private final String label;
    @SuppressWarnings("FieldMayBeFinal") // can java shut up pls
    private List<String> answer_choices; // if there are any

    public Question(int id, String type, String label, List<String> options) {
        this.identifier = id;
        this.type = type;
        this.label = label;
        this.answer_choices = options;
    }

    public int getIdentifier(){ return identifier; }
    public String getQType(){ return type; }
    public String getQLabel(){ return label; }
    public List<String> getOptions(){ return answer_choices; }

}
