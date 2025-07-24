package com.example.preely.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.preely.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<String> imageList;
    private final OnImageRemoveListener removeListener;

    public interface OnImageRemoveListener {
        void onRemove(int position);
    }

    public ImageAdapter(List<String> imageList, OnImageRemoveListener removeListener) {
        this.imageList = imageList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String image = imageList.get(position);
        Glide.with(holder.imageView.getContext())
                .load(image)
                .placeholder(R.drawable.ic_image)
                .into(holder.imageView);
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) removeListener.onRemove(position);
        });
    }

    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnRemove;
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_preview);
            btnRemove = itemView.findViewById(R.id.btn_remove_image);
        }
    }
} 