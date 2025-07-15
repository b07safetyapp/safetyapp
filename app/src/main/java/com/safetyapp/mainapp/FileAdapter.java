package com.safetyapp.mainapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String fileName);
    }

    @SuppressWarnings("FieldMayBeFinal")
    private List<String> fileNames;
    private final OnItemClickListener listener;

    public FileAdapter(List<String> fileNames, OnItemClickListener listener) {
        this.fileNames = fileNames;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileTitle;

        public ViewHolder(View view) {
            super(view);
            fileTitle = view.findViewById(android.R.id.text1);
        }

        public void bind(String name, OnItemClickListener listener) {
            fileTitle.setText(name);
            fileTitle.setOnClickListener(v -> listener.onItemClick(name));
        }
    }

    @NonNull
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileAdapter.ViewHolder holder, int position) {
        holder.bind(fileNames.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }
}
