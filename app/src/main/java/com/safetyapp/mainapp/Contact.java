package com.safetyapp.mainapp;

public class Contact {
    public String name;
    public String relationship;
    public String phone;
    public String id; // FireBase key

    public Contact(){}
    public Contact(String id, String name, String relation, String phone){
        this.name = name;
        this.relationship = relation;
        this.phone = phone;
        this.id = id;
    }
}
