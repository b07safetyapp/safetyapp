package com.safetyapp.mainapp;

import java.util.List;

public class FileItem {
    public String docId;      // Firestore document ID
    public String url;        // Download URL from Storage
    public String type;       // MIME type
    public String uploadedBy; // Uploader's UID
    public List<String> tags; // Tags list

    // Firestore needs empty constructor
    public FileItem() {}

    // You can add constructors/getters/setters if you want
}
