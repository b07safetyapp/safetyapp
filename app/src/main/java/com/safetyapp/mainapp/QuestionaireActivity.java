package com.safetyapp.mainapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class QuestionaireActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
        setTitle("Questionaire Activity");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // get the items
        QuestionPresenter questionPresenter = new QuestionPresenter();
        ArrayList<QuestionChoiceModel> questions = new ArrayList<>();
        questions.add(questionPresenter.getcurrentquestion());
        QuestionaireAdapter adapter = new QuestionaireAdapter(this, questions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}
