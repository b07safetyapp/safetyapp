package com.safetyapp.mainapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.MyViewHolder>{
    Context ctx;
    ArrayList<String> tips;
    ArrayList<Boolean> checks;

    public PlanAdapter(Context ctx){
        this.ctx = ctx;
        PlanPresenter presenter = new PlanPresenter();
        this.tips = presenter.getTipsarr();
        this.checks = presenter.getChecks();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.item_text_check, parent, false);
        return new PlanAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanAdapter.MyViewHolder holder, int position) {
        holder.text.setText(this.tips.get(position));
        holder.checkbox.setChecked(this.checks.get(position));
    }

    @Override
    public int getItemCount() {
        return this.checks.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView text;
        CheckBox checkbox;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.textFileName);
            checkbox = itemView.findViewById(R.id.check);
        }
    }
}
