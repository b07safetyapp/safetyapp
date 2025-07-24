package com.safetyapp.mainapp;

import java.util.List;

public class SupportResource {
    private final String name;
    private final String category;
    private final String contact;
    private final String website;
    private final List<Double> location; // New field for location


    public SupportResource(String name, String category, String contact, String website, List<Double> location) {
        this.name = name;
        this.category = category;
        this.contact = contact;
        this.website = website;
        this.location = location;
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

    public List<Double> getLocation() {
        return location;
    }


}