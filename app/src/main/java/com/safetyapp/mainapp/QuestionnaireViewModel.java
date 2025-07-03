package com.safetyapp.mainapp;               // 1. Package declaration

import android.app.Application;                           // 2. Application context
import androidx.annotation.NonNull;                        // 3. Non-null annotation
import androidx.lifecycle.AndroidViewModel;               // 4. ViewModel with Application scope
import androidx.lifecycle.LiveData;                        // 5. LiveData for observing data
import androidx.lifecycle.MutableLiveData;                 // 6. Mutable LiveData


import java.util.HashMap;                                  // 9. HashMap implementation
import java.util.List;                                     // 10. List interface
import java.util.Map;                                      // 11. Map interface

/**
 * ViewModel that holds survey questions and user answers.
 * Survives configuration changes and provides data to the UI.
 */
public class QuestionnaireViewModel extends AndroidViewModel { // 12. ViewModel class

    private QuestionsRepository repository;               // 13. Repository instance
    private MutableLiveData<List<Question>> questions;    // 14. LiveData for question list
    private LiveData<Map<String, Object>> answers;        // 15. LiveData for answers map

    /**
     * Constructor initializes repository and LiveData.
     * @param application Application context
     */
    public QuestionnaireViewModel(@NonNull Application application) {
        super(application);
        // 16. Instantiate repository with application context
        repository = new QuestionsRepository(application);

        // 17. Prepare MutableLiveData for questions
        questions = new MutableLiveData<>();
        // 18. Get answers LiveData from repository
        answers = repository.getAnswers();
    }

    /**
     * Expose questions LiveData for UI observation.
     * @return LiveData of List<Question>
     */
    public LiveData<List<Question>> getQuestions() {
        return questions;
    }

    /**
     * Expose answers LiveData for UI observation.
     * @return LiveData of Map<QuestionId, answer>
     */
    public LiveData<Map<String, Object>> getAnswers() {
        return answers;
    }

    /**
     * Load a survey branch starting at a given question key.
     * @param startKey ID of the first question (e.g., "status")
     */
    public void startBranch(String startKey) {
        // 19. Obtain current answers snapshot or empty map
        Map<String, Object> currentAnswers = answers.getValue() != null ?
                answers.getValue() : new HashMap<>();
        // 20. Load the branch from repository based on answers
        List<Question> branchQuestions = repository.loadBranch(startKey, currentAnswers);
        // 21. Post loaded questions to LiveData
        questions.postValue(branchQuestions);
    }

    /**
     * Save a single answer and trigger repository write.
     * @param id   Question identifier
     * @param ans  User's answer
     */
    public void saveAnswer(String id, Object ans) {
        repository.saveAnswer(id, ans);
    }
}
