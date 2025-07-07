/*
package com.safetyapp.mainapp;               // 1. Package declaration

import android.app.Application;                           // 2. Application context
import androidx.annotation.NonNull;                        // 3. Non-null annotation
import androidx.lifecycle.AndroidViewModel;               // 4. ViewModel with Application scope
import androidx.lifecycle.LiveData;                        // 5. LiveData for observing data
import androidx.lifecycle.MutableLiveData;                 // 6. Mutable LiveData


import java.util.HashMap;                                  // 9. HashMap implementation
import java.util.List;                                     // 10. List interface
import java.util.Map;                                      // 11. Map interface

public class TEMPQuestionnaireViewModel extends AndroidViewModel { // 12. ViewModel class

    private QuestionsRepository repository;               // 13. Repository instance
    private MutableLiveData<List<Question>> questions;    // 14. LiveData for question list
    private LiveData<Map<String, Object>> answers;        // 15. LiveData for answers map

    public TEMPQuestionnaireViewModel(@NonNull Application application) {
        super(application);
        // 16. Instantiate repository with application context
        repository = new QuestionsRepository(application);

        // 17. Prepare MutableLiveData for questions
        questions = new MutableLiveData<>();
        // 18. Get answers LiveData from repository
        answers = repository.getAnswers();
    }

    public LiveData<List<Question>> getQuestions() {
        return questions;
    }

    public LiveData<Map<String, Object>> getAnswers() {
        return answers;
    }

    public void startBranch(String startKey) {
        // 19. Obtain current answers snapshot or empty map
        Map<String, Object> currentAnswers = answers.getValue() != null ?
                answers.getValue() : new HashMap<>();
        // 20. Load the branch from repository based on answers
        List<Question> branchQuestions = repository.loadBranch(startKey, currentAnswers);
        // 21. Post loaded questions to LiveData
        questions.postValue(branchQuestions);
    }

    public void saveAnswer(String id, Object ans) {
        repository.saveAnswer(id, ans);
    }
}


*/