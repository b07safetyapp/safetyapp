package com.safetyapp.mainapp;

public class Contact {
    public String name;
    public String relationship;
    public String phone;

    public Contact(){
        this.name = "LeBron James";
        this.relationship = "King James";
        this.phone = "+1 234 567 8901";
    }

    public Contact(String name, String relation, String phone){
        this.name = name;
        this.relationship = relation;
        this.phone = phone;
    }
}
