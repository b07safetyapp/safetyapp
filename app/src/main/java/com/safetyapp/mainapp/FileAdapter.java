package com.safetyapp.mainapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String fileName);
    }

    private List<String> fileNames;
    private Map<String, List<String>> fileTags;  // fileName -> tags
    private OnItemClickListener listener;

    public FileAdapter(List<String> fileNames, Map<String, List<String>> fileTags, OnItemClickListener listener) {
        this.fileNames = fileNames;
        this.fileTags = fileTags;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileTitle;
        LinearLayout tagContainer;

        public ViewHolder(View view) {
            super(view);
            fileTitle = view.findViewById(R.id.textFileName);
            tagContainer = view.findViewById(R.id.tagContainer);
        }

        public void bind(String fileName, List<String> tags, OnItemClickListener listener) {
            fileTitle.setText(fileName);
            fileTitle.setOnClickListener(v -> listener.onItemClick(fileName));

            tagContainer.removeAllViews();
            if (tags != null) {
                for (String tag : tags) {
                    TextView tagView = new TextView(tagContainer.getContext());
                    tagView.setText(tag);
                    tagView.setTextColor(Color.WHITE);
                    tagView.setTextSize(12);
                    tagView.setPadding(20, 8, 20, 8);
                    tagView.setBackgroundResource(R.drawable.tag_background);
                    tagView.setAlpha(1f);
                    GradientDrawable bg = (GradientDrawable) tagView.getBackground();
                    switch (tag) {
                        case "A": bg.setColor(Color.RED); break;
                        case "B": bg.setColor(Color.BLUE); break;
                        case "C": bg.setColor(Color.GREEN); break;
                        default: bg.setColor(Color.GRAY);
                    }
                    tagContainer.addView(tagView);
                }
            }
        }
    }

    @NonNull
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_with_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileAdapter.ViewHolder holder, int position) {
        String fileName = fileNames.get(position);
        List<String> tags = fileTags.get(fileName.replace(".", "_"));
        holder.bind(fileName, tags, listener);
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }
}
