package com.safetyapp.mainapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MenuFragment extends Fragment {
    AppContext appcontext;
    Context ctx;
    public MenuFragment() {
        appcontext = new AppContext();
        ctx = appcontext.getContext();
    }

    public static MenuFragment newInstance() {
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        // Set up click listeners
        Button homeButton = view.findViewById(R.id.button3);
        Button questionaireButton = view.findViewById(R.id.button2);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("going", "going to home page");
                // route to homepage
                Intent i = new Intent(ctx, HomeActivity.class);
                i.putExtra("mykey", "myvalue");
                startActivity(i);
            }
        });

        questionaireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("going", "going to home page");
                // route to homepage
                Intent i = new Intent(ctx, QuestionaireActivity.class);
                i.putExtra("mykey", "myvalue");
                startActivity(i);
            }
        });

        return view;
    }

}
