package com.safetyapp.mainapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

class QuestionaireAdapter extends RecyclerView.Adapter<QuestionaireAdapter.MyViewHolder>{
    QuestionPresenter questionPresenter;
    Context context;
    ArrayList<QuestionChoiceModel> questionchoices;
    ArrayList<String> choices;

    public QuestionaireAdapter(Context context){
        this.context = context;
        this.questionPresenter = new QuestionPresenter();
        this.questionchoices = new ArrayList<>();
        this.questionchoices.add(questionPresenter.getcurrentquestion());
        this.choices = questionPresenter.currentchoices;
    }

    @NonNull
    @Override
    public QuestionaireAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_multi, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionaireAdapter.MyViewHolder holder, int position) {
        // current question
        Log.d("current is:", Integer.toString(position));
        Log.d("lets see all of the questions we have:", "");
        for (int i = 0; i < questionchoices.size(); i++){
            Log.d("question "+ i, questionchoices.get(i).getLabel());
        }
        QuestionChoiceModel currentquestion = questionchoices.get(position);
        holder.label.setText(currentquestion.getLabel());

        ArrayList<String> options = currentquestion.options;
        // add chip groups
        for (int i = 0; i < options.size(); i++){
            Log.d("adding ", "");
            Chip chip = (Chip) LayoutInflater.from(context).inflate(R.layout.chip_choice, holder.chipGroup, false); // 85. Create chip
            chip.setText(options.get(i)); // 86. Set chip text
            chip.setChecked(choices.get(position).equals(options.get(i))); // 87. Check if selected
            chip.setOnCheckedChangeListener((c, checked) -> { // 88. Listen toggle
                // check if this is the final one
                Log.d("currently this option is:", c.getText().toString());
                if (c.getText().toString().equals("leave")){
                    Log.d("GOING TO HOME!!", "");
                    gohomepage();
                }
                else if(c.isChecked()){
                    Log.d("click", c.getText().toString());
                    String targetoption = c.getText().toString();
                    // modify the current question
                    // clear all the other checks

                    questionPresenter.changechoice(currentquestion.id, targetoption);
                    questionPresenter.addquestion(currentquestion.id, targetoption);
                    // get the next question
                    this.choices = questionPresenter.currentchoices;
                    Log.d("questions:", questionPresenter.currentquestions.toString());
                    Log.d("choices:", choices.toString());
                    questionchoices.add(questionPresenter.getcurrentquestion());
                    Log.d("question choices:", Integer.toString(questionchoices.size()));
                    notifyItemInserted(questionchoices.size() - 1);
                }
            });
            chip.setWidth(3000);
            holder.chipGroup.addView(chip);
        }
    }
    @Override
    public int getItemCount() {
        return questionchoices.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView label;
        ChipGroup chipGroup;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            chipGroup = itemView.findViewById(R.id.chipGroup);
        }
    }

    public void gohomepage(){
        Log.d("going", "going to home page");
        // route to homepage
        Intent i = new Intent(context, HomeActivity.class);
        i.putExtra("mykey", "myvalue");
        context.startActivity(i);
    }
}