package com.example.audiorecorder.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiorecorder.R;


import java.io.File;

public class AudioFileAdapter extends RecyclerView.Adapter<AudioFileAdapter.FileViewHolder> {

    private File[] mFiles;
    private IClickListener mClickListener;

    AudioFileAdapter(File[] files, IClickListener clickListener) {
        mFiles = files;
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_text_view, parent, false);
        return new FileViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(mFiles[position], position);
    }

    @Override
    public int getItemCount() {
        if (mFiles == null) {
            return 0;
        }
        return mFiles.length;
    }

    void updateAdapter(File[] files) {
        mFiles = files;
        notifyDataSetChanged();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextView;
        private IClickListener mClickListener;

        FileViewHolder(@NonNull View itemView, IClickListener clickListener) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.file_text_view);

            mClickListener = clickListener;
        }

        void bind(final File file, final int position) {
            mTextView.setText(file.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onClick(position);
                }
            });
        }
    }
}
