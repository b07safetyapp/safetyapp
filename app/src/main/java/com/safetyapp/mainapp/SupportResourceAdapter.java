package com.safetyapp.mainapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class SupportResourceAdapter extends RecyclerView.Adapter<SupportResourceAdapter.ViewHolder> {


    private final List<SupportResource> resourceList;


    public SupportResourceAdapter(List<SupportResource> resourceList) {
        this.resourceList = resourceList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_support_resource, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SupportResource resource = resourceList.get(position);

        holder.nameTextView.setText(resource.getName());
        holder.categoryTextView.setText(resource.getCategory());
        holder.contactTextView.setText(resource.getContact());
    }


    @Override
    public int getItemCount() {

        return resourceList != null ? resourceList.size() : 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView categoryTextView;
        TextView contactTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.textViewName);
            categoryTextView = itemView.findViewById(R.id.textViewCategory);
            contactTextView = itemView.findViewById(R.id.textViewContact);
        }
    }
}