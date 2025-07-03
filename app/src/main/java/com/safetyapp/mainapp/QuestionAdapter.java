package com.safetyapp.mainapp; // 1. Package where this adapter lives

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> { // 18. Adapter class for RecyclerView

    public interface AnswerListener { // 20. Listener to propagate answers
        void onAnswer(String questionId, Object answer); // 21. Callback with question ID and answer
    }

    private List<Question> questions; // 22. List of questions to display
    private Map<String, Object> answers; // 23. Current answers map
    @SuppressWarnings("FieldMayBeFinal")
    private AnswerListener listener; // 24. Listener instance

    public QuestionsAdapter(List<Question> questions, Map<String, Object> answers, AnswerListener listener) { // 25. Constructor
        this.questions = questions; // 26. Initialize questions
        this.answers = answers; // 27. Initialize answers
        this.listener = listener; // 28. Initialize listener
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateQuestions(List<Question> qs) { // 29. Update question list dynamically
        this.questions = qs; // 30. Set new questions
        notifyDataSetChanged(); // 31. Refresh RecyclerView
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateAnswers(Map<String, Object> ans) { // 32. Update answers dynamically
        this.answers = ans; // 33. Set new answers
        notifyDataSetChanged(); // 34. Refresh RecyclerView
    }

    @Override // 35. Override method
    public int getItemCount() { // 36. Total items
        return questions.size(); // 37. Return question count
    }

    @Override // 38. Override method
    public int getItemViewType(int position) { // 39. Determine view type per position
        String type = questions.get(position).getQType(); // 40. Get question type
        switch (type) { // 41. Switch on type
            case "rating": return 1; // 42. Rating view
            case "boolean": return 2; // 43. Boolean view
            case "multi": return 3; // 44. Multi-choice view
            default: return 0; // 45. Default to text view
        }
    }

    @NonNull // 46. Non-null return
    @Override // 47. Override method
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // 48. Create ViewHolder
        LayoutInflater inflater = LayoutInflater.from(parent.getContext()); // 49. Get inflater

        if (viewType == 1) { // 50. Rating type
            View v = inflater.inflate(R.layout.item_rating, parent, false); // 51. Inflate rating layout
            return new RatingHolder(v); // 52. Return rating holder
        } else if (viewType == 2) { // 53. Boolean type
            View v = inflater.inflate(R.layout.item_boolean, parent, false); // 54. Inflate boolean layout
            return new BooleanHolder(v); // 55. Return boolean holder
        } else if (viewType == 3) { // 56. Multi-choice type
            View v = inflater.inflate(R.layout.item_multi, parent, false); // 57. Inflate multi layout
            return new MultiHolder(v); // 58. Return multi holder
        } else { // 59. Default text
            View v = inflater.inflate(R.layout.item_text, parent, false); // 60. Inflate text layout
            return new TextHolder(v); // 61. Return text holder
        }
    }

    @Override // 62. Override method
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) { // 63. Bind data to ViewHolder
        Question q = questions.get(position); // 64. Get current question
        Object existing = answers.get(q.getIdentifier()); // 65. Fetch existing answer

        if (holder instanceof TextHolder) { // 66. Text or info type
            TextHolder h = (TextHolder) holder; // 67. Cast holder
            h.label.setText(q.getLabel()); // 68. Set label text
            if (existing != null) {
                h.edit.setText(existing.toString()); // 69. Pre-fill answer
            }
            h.edit.addTextChangedListener(new SimpleTextWatcher() { // 70. Listen text changes
                @Override
                public void onTextChanged(String s) { // 71. On change
                    listener.onAnswer(q.getId(), s); // 72. Send answer
                }
            });

        } else if (holder instanceof RatingHolder) { // 73. Rating type
            RatingHolder h = (RatingHolder) holder; // 74. Cast holder
            h.label.setText(q.getLabel()); // 75. Set label
            if (existing instanceof Number) {
                h.bar.setRating(((Number) existing).floatValue()); // 77. Set rating
            }
            h.bar.setOnRatingBarChangeListener((rb, rating, fromUser) -> { // 78. Rating listener
                if (fromUser) {
                    listener.onAnswer(q.getId(), rating); // 79. Send answer
                }
            });

        } else if (holder instanceof MultiHolder) { // 80. Multi-choice type
            MultiHolder h = (MultiHolder) holder; // 81. Cast holder
            h.chipGroup.removeAllViews(); // 82. Clear old chips
            h.label.setText(q.getLabel()); // 83. Set label

            for (String opt : q.getOptions()) { // 84. For each option
                Chip chip = (Chip) LayoutInflater.from(h.chipGroup.getContext())
                        .inflate(R.layout.chip_choice, h.chipGroup, false); // 85. Create chip
                chip.setText(opt); // 86. Set chip text
                chip.setChecked(existing instanceof List && ((List<?>) existing).contains(opt)); // 87. Check if selected
                chip.setOnCheckedChangeListener((c, checked) -> { // 88. Listen toggle
                    List<String> sel = new ArrayList<>(); // 89. Build selection list
                    for (int i = 0; i < h.chipGroup.getChildCount(); i++) { // 90. Iterate chips
                        Chip cc = (Chip) h.chipGroup.getChildAt(i); // 91. Get chip
                        if (cc.isChecked()) {
                            sel.add(cc.getText().toString()); // 92. Add checked
                        }
                    }
                    listener.onAnswer(q.getId(), sel); // 93. Send answer list
                });
                h.chipGroup.addView(chip); // 94. Add chip to group
            }

        } else if (holder instanceof BooleanHolder) { // 95. Boolean type
            BooleanHolder h = (BooleanHolder) holder; // 96. Cast holder
            h.label.setText(q.getLabel()); // 97. Set label text
            if (existing instanceof Boolean) {
                h.switcher.setChecked((Boolean) existing); // 99. Set switch state
            }
            h.switcher.setOnCheckedChangeListener((buttonView, isChecked) -> { // 100. Listen toggle
                listener.onAnswer(q.getId(), isChecked); // 101. Send answer
            });
        }
    }

    // ViewHolder for text input questions
    static class TextHolder extends RecyclerView.ViewHolder {
        TextView label; // 102. Label view
        EditText edit; // 103. Text input

        TextHolder(View itemView) { // 104. Constructor
            super(itemView);
            label = itemView.findViewById(R.id.label); // 105. Bind label
            edit = itemView.findViewById(R.id.editText); // 106. Bind EditText
        }
    }

    // ViewHolder for rating questions
    static class RatingHolder extends RecyclerView.ViewHolder {
        TextView label; // 107. Label view
        RatingBar bar; // 108. RatingBar

        RatingHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label); // 109. Bind label
            bar = itemView.findViewById(R.id.ratingBar); // 110. Bind RatingBar
        }
    }

    // ViewHolder for multi-choice questions
    static class MultiHolder extends RecyclerView.ViewHolder {
        TextView label; // 111. Label view
        ChipGroup chipGroup; // 112. ChipGroup

        MultiHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label); // 113. Bind label
            chipGroup = itemView.findViewById(R.id.chipGroup); // 114. Bind ChipGroup
        }
    }

    // ViewHolder for boolean (switch) questions
    static class BooleanHolder extends RecyclerView.ViewHolder {
        TextView label; // 115. Label view
        SwitchCompat switcher; // 116. SwitchCompat widget

        BooleanHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label); // 117. Bind label
            switcher = itemView.findViewById(R.id.switchWidget); // 118. Bind SwitchCompat
        }
    }

    // SimpleTextWatcher to listen to text changes without boilerplate
    abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        public abstract void onTextChanged(String s); // 119. Abstract text-change callback
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {} // 120. No-op
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { // 121. Forward change
            onTextChanged(s.toString());
        }
        @Override public void afterTextChanged(android.text.Editable s) {} // 122. No-op
    }
}
