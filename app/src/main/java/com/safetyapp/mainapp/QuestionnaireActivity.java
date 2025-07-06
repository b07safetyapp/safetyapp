package com.safetyapp.mainapp;             // 1. Package declaration

import android.os.Bundle;                              // 2. Android bundle
import androidx.annotation.Nullable;                    // 3. Nullable annotations
import androidx.appcompat.app.AppCompatActivity;         // 4. Base activity class
import androidx.lifecycle.ViewModelProvider;            // 5. ViewModel provider
import androidx.recyclerview.widget.LinearLayoutManager; // 6. LayoutManager for RecyclerView
import androidx.recyclerview.widget.RecyclerView;       // 7. RecyclerView
import com.safetyapp.mainapp.R;                          // 8. Resource reference


public class QuestionnaireActivity extends AppCompatActivity { //11. Activity class

    private QuestionnaireViewModel viewModel;           //12. Reference to ViewModel
    private QuestionAdapter adapter;                   //13. Adapter instance

    @Override                                         //14. Override lifecycle method
    protected void onCreate(@Nullable Bundle savedInstanceState) { //15. onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire); //16. Set layout

        //17. Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //18. Vertical list

        //19. Instantiate ViewModel
        viewModel = new ViewModelProvider(this).get(QuestionnaireViewModel.class);

        //20. Observe answers LiveData
        viewModel.getAnswers().observe(this, answers -> {
            if (adapter != null) {                      //21. If adapter exists
                adapter.updateAnswers(answers);       //22. Update answers
            }
        });

        //23. Observe questions LiveData
        viewModel.getQuestions().observe(this, questions -> {
            if (adapter == null) {                     //24. First time setup
                adapter = new QuestionAdapter(questions,
                        viewModel.getAnswers().getValue(), //25. Initial answers
                        (id, ans) -> viewModel.saveAnswer(id, ans)); //26. Save callback
                recyclerView.setAdapter(adapter);     //27. Bind adapter
            } else {
                adapter.updateQuestions(questions);   //28. Questions changed (branch switch)
            }
        });

        //29. Kick off survey at starting key 'status'
        viewModel.startBranch("status");
    }
}
