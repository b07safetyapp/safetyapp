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

class QuestionaireAdapter extends RecyclerView.Adapter<QuestionaireAdapter.MyViewHolder>{
    Context context;
    ArrayList<QuestionChoiceModel> questionchoices;

    public QuestionaireAdapter(Context context, ArrayList<QuestionChoiceModel> questionchoices){
        this.context = context;
        this.questionchoices = questionchoices;

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
        holder.label.setText(questionchoices.get(position).getLabel());
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
}