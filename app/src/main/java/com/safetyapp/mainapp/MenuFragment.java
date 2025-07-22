package com.safetyapp.mainapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MenuFragment extends Fragment {

    public MenuFragment() {
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
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        Button btn1 = root.findViewById(R.id.button1);
        Button btn2 = root.findViewById(R.id.button2);
        Button btn3 = root.findViewById(R.id.button3);
        Button btn4 = root.findViewById(R.id.button4);
        Button btn5 = root.findViewById(R.id.button5);
        Button btn6 = root.findViewById(R.id.button6);

        btn1.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        btn2.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), QuestionaireActivity.class);
            startActivity(i);
        });
        btn3.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), HomeActivity.class);
            startActivity(i);
        });
        btn4.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), SupportConnectionActivity.class);
            startActivity(i);
        });
        btn5.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), PlanActivity.class);
            startActivity(i);
        });
//        btn6.setOnClickListener(v -> {
//            Intent i = new Intent(getActivity(), Screen6Activity.class);
//            startActivity(i);
//        });

        return root;

    }

}
