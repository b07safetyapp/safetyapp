package com.safetyapp.mainapp;

import androidx.fragment.app.Fragment;

/*
 * USE:
 * <fragment
 *     android:id="@+id/topMenuBarFragment"
 *     android:name="com.safetyapp.mainapp.TopMenuBarFragment"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;

public class TopMenuBarFragment extends Fragment {

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_topmenubar, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Button 1 - Settings Activity
        Button button1 = view.findViewById(R.id.settings);
        button1.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class); // make this later
            startActivity(intent);
        });

        // Button 2 - Logout
        Button button2 = view.findViewById(R.id.logout);
        button2.setOnClickListener(v -> logoutUser());

        // Button 3 - Exit Activity
        Button button3 = view.findViewById(R.id.exitbtn);
        button3.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ExitActivity.class); // whatever this is called
            startActivity(intent);
        });

        return view;
    }

    private void logoutUser() {
        if (mAuth != null) {
            mAuth.signOut();
            // Redirect to login activity or main activity after logout
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}