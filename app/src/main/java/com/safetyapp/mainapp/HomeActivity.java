package com.safetyapp.mainapp;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends BaseActivity {
    @Override
    protected int getContentLayoutId() {
        return R.layout.home_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Home Activity");
        Fragment myfragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_box, myfragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_box, myfragment).addToBackStack(null).commit();
    }
}
