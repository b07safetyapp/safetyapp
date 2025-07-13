package com.safetyapp.mainapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ContactAdapter adapter;
    List<Contact> contactList;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
    }
}
