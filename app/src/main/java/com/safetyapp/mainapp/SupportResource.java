package com.safetyapp.mainapp;

public class SupportResource {
    private final String name;
    private final String category;
    private final String contact;
    private final String website;


    public SupportResource(String name, String category, String contact, String website) {
        this.name = name;
        this.category = category;
        this.contact = contact;
        this.website = website;
    }

    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }

    public String getContact() {
        return contact;
    }

    public String getWebsite() {
        return website;
    }


}