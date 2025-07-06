package com.safetyapp.mainapp;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Home Activity");
        Fragment myfragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_box, myfragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_box, myfragment).addToBackStack(null).commit();
    }
}
