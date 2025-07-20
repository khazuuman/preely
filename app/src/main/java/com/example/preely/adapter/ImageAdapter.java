package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.entities.Image;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Image> imageList = new ArrayList<>();
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(Image image);
        void onImageEdit(Image image);
        void onImageDelete(Image image);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Image image = imageList.get(position);
        holder.bind(image);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvPostId;
        private TextView tvImageLink;
        private ImageButton btnEdit;
        private ImageButton btnDelete;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvPostId = itemView.findViewById(R.id.tv_image_name);
            tvImageLink = itemView.findViewById(R.id.tv_image_size);
            btnEdit = itemView.findViewById(R.id.btn_edit_image);
            btnDelete = itemView.findViewById(R.id.btn_delete_image);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onImageClick(imageList.get(position));
                }
            });

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onImageEdit(imageList.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onImageDelete(imageList.get(position));
                }
            });
        }

        public void bind(Image image) {
            tvPostId.setText("Post ID: " + (image.getPost_id() != null ? image.getPost_id() : "N/A"));
            
            // Show truncated link
            if (image.getLink() != null && !image.getLink().isEmpty()) {
                String link = image.getLink();
                if (link.length() > 30) {
                    link = link.substring(0, 30) + "...";
                }
                tvImageLink.setText("Link: " + link);
            } else {
                tvImageLink.setText("Link: N/A");
            }
            
            // TODO: Load image using Glide or Picasso
            // For now, set a placeholder
            ivImage.setImageResource(R.drawable.img);
        }
    }
} 