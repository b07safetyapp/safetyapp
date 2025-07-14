package com.safetyapp.mainapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

class QuestionaireAdapter extends RecyclerView.Adapter<QuestionaireAdapter.MyViewHolder>{
    private static final int VIEW_TYPE_MULTI= 0;
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_INFO = 2;
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
        // switch case for the type of the item
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        switch(viewType){
            case VIEW_TYPE_TEXT:
                view = inflater.inflate(R.layout.item_text, parent, false);
                break;
            case VIEW_TYPE_MULTI:
                view = inflater.inflate(R.layout.item_multi, parent, false);
                break;
            case VIEW_TYPE_INFO:
                view = inflater.inflate(R.layout.item_multi, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.item_multi, parent, false);
                break;
        }

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionaireAdapter.MyViewHolder holder, int position) {
        int quesiontype = getItemViewType(position);
        switch (quesiontype){
            case VIEW_TYPE_TEXT:
                onBindTextChoices(holder, position);
                break;
            case VIEW_TYPE_INFO:
                onBindMultiChoices(holder, position);
                break;
            case VIEW_TYPE_MULTI:
                onBindMultiChoices(holder, position);
                break;
            default:
                onBindMultiChoices(holder, position);
                break;
        }
    }

    public void onBindTextChoices(@NonNull QuestionaireAdapter.MyViewHolder holder, int position){
        QuestionChoiceModel currentquestion = questionchoices.get(position);
        holder.label.setText(currentquestion.getLabel());
        EditText textinput = holder.editText;
        textinput.setText(choices.get(position));
        // add the onclick method for the button
        Button submitbutton = holder.submitButton;
        //create a variable that contain your button
        submitbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                questionPresenter.changechoice(currentquestion.id, textinput.getText().toString());
                questionPresenter.addquestiontext(currentquestion.id);
                questionchoices.add(questionPresenter.getcurrentquestion());
                choices = questionPresenter.currentchoices;
                int questionsize = questionchoices.size()-1;
                notifyItemRangeChanged(position, questionsize);
                notifyItemInserted(questionchoices.size() - 1);
            }
        });
    }

    public void onBindMultiChoices(@NonNull QuestionaireAdapter.MyViewHolder holder, int position){
        // current question
        QuestionChoiceModel currentquestion = questionchoices.get(position);
        holder.label.setText(currentquestion.getLabel());
        holder.chipGroup.removeAllViews();

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
                        // remove all
                        notifyItemRemoved(v);
                        this.questionchoices.remove(v);
                    }
                    notifyItemRangeChanged(position, questionsize);
                    // get the next question
                    questionchoices.add(questionPresenter.getcurrentquestion());
                    this.choices = questionPresenter.currentchoices;
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
        EditText editText;
        Button submitButton;
        TextView label;
        ChipGroup chipGroup;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            chipGroup = itemView.findViewById(R.id.chipGroup);
            editText = itemView.findViewById(R.id.editText);
            submitButton = itemView.findViewById(R.id.submit_area);

        }
    }

    public void gohomepage(){
        // tell the presenter to save the answer content into a json
        questionPresenter.saveResultsToJson();
        // route to homepage
        Intent i = new Intent(context, HomeActivity.class);
        i.putExtra("mykey", "myvalue");
        context.startActivity(i);
    }

    @Override
    public int getItemViewType(int position) {
        QuestionChoiceModel currentQuestion = questionchoices.get(position);

        // Determine view type based on question properties
        // You'll need to add a field to QuestionChoiceModel to specify type
        switch (currentQuestion.getType()) {
            case "multi":
                return VIEW_TYPE_MULTI;
            case "text":
                return VIEW_TYPE_TEXT;
            case "info":
                return VIEW_TYPE_INFO;
            default:
                return VIEW_TYPE_MULTI;
        }
    }
}