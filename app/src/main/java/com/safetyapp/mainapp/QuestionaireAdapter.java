package com.safetyapp.mainapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class QuestionaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private AppContext appcontext;
    private Context ctx;

    public QuestionaireAdapter(){
        this.appcontext = new AppContext();
        this.ctx = appcontext.getContext();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
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

    @Override // 62. Override method
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) { // 63. Bind data to ViewHolder
        QuestionPresenter questionPresenter = new QuestionPresenter();
        try{
            questionPresenter.loadquestionsfromjson();
        }catch (Exception e){
            System.err.println(e);
        }

        QuestionChoiceModel q = questionPresenter.getcurrentquestion();
        String existing = q.getChoice(); // 65. Fetch existing answer

        MultiHolder h = (MultiHolder) holder; // 81. Cast holder
        h.chipGroup.removeAllViews(); // 82. Clear old chips
        h.label.setText(q.getLabel()); // 83. Set label

        for (String opt : q.getOptions()) { // 84. For each option
            Chip chip = (Chip) LayoutInflater.from(h.chipGroup.getContext())
                    .inflate(R.layout.chip_choice, h.chipGroup, false); // 85. Create chip
            chip.setText(opt); // 86. Set chip text
            chip.setChecked(existing.equals(opt)); // 87. Check if selected
            chip.setOnCheckedChangeListener((c, checked) -> { // 88. Listen toggle
                List<String> sel = new ArrayList<>(); // 89. Build selection list
                for (int i = 0; i < h.chipGroup.getChildCount(); i++) { // 90. Iterate chips
                    Chip cc = (Chip) h.chipGroup.getChildAt(i); // 91. Get chip
                    if (cc.isChecked()) {
                        sel.add(cc.getText().toString()); // 92. Add checked
                    }
                }
            });
            h.chipGroup.addView(chip); // 94. Add chip to group
        }


    }

    @Override
    public int getItemCount() {
        return 0;
    }

}