package com.safetyapp.mainapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    public interface OnFileDeleteListener {
        void onDeleteClick(FileItem fileItem);
    }

    private final List<FileItem> files;
    private final OnFileDeleteListener listener;

    public FileAdapter(List<FileItem> files, OnFileDeleteListener listener) {
        this.files = files;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = files.get(position);
        holder.fileName.setText(fileItem.url.substring(fileItem.url.lastIndexOf('/') + 1));
        holder.fileType.setText(fileItem.type != null ? fileItem.type : "Unknown");

        String tagsDisplay = "Tags: ";
        if (fileItem.tags != null && !fileItem.tags.isEmpty()) {
            tagsDisplay += String.join(", ", fileItem.tags);
        } else {
            tagsDisplay += "None";
        }
        holder.fileTags.setText(tagsDisplay);

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(fileItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileType, fileTags;
        ImageButton deleteBtn;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.textFileName);
            fileType = itemView.findViewById(R.id.textFileType);
            fileTags = itemView.findViewById(R.id.textFileTags);
            deleteBtn = itemView.findViewById(R.id.btnDeleteFile);
        }
    }
}
