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
        // print the current size of things
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
        Log.d("lets see all of the questions we have:", "");
        for (int i = 0; i < questionchoices.size(); i++){
            Log.d("question "+ i, questionchoices.get(i).getLabel());
        }
        QuestionChoiceModel currentquestion = questionchoices.get(position);
        holder.label.setText(currentquestion.getLabel());

        ArrayList<String> options = currentquestion.options;
        // add chip groups
        for (int i = 0; i < options.size(); i++){
            Chip chip = (Chip) LayoutInflater.from(context).inflate(R.layout.chip_choice, holder.chipGroup, false); // 85. Create chip
            chip.setText(options.get(i)); // 86. Set chip text
            chip.setChecked(choices.get(position).equals(options.get(i))); // 87. Check if selected
            chip.setOnCheckedChangeListener((c, checked) -> { // 88. Listen toggle
                // clear the checked status for all other chips
                // go back home, if this chip is the one that makes yu leave
                if (c.getText().toString().equals("leave")){
                    gohomepage();
                }
                else if(c.isChecked()){
                    String targetoption = c.getText().toString();
                    // modify the current question
                    // clear all the other checks

                    questionPresenter.changechoice(currentquestion.id, targetoption);
                    questionPresenter.addquestion(currentquestion.id, targetoption);
                    // delete all previous question and choices that are after this index.
                    int questionsize = questionchoices.size()-1;
                    for (int v = questionchoices.size()-1; v > position; v--){
                        Log.d("frontend removing:", Integer.toString(v));
                        // remove all
                        notifyItemRemoved(v);
                        this.questionchoices.remove(v);
                    }
                    notifyItemRangeChanged(position, questionsize);
                    questionPresenter.logcurrent();
                    // get the next question
                    questionchoices.add(questionPresenter.getcurrentquestion());
                    this.choices = questionPresenter.currentchoices;
                    Log.d("!!! question choices:", questionchoices.toString());
                    Log.d("!!! choices:", choices.toString());
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
        // route to homepage
        Intent i = new Intent(context, HomeActivity.class);
        i.putExtra("mykey", "myvalue");
        context.startActivity(i);
    }
}