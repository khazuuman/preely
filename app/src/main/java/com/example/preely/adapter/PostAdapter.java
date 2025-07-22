package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.entities.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList = new ArrayList<>();
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
        void onPostEdit(Post post);
        void onPostDelete(Post post);
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPostTitle;
        private TextView tvPostDescription;
        private TextView tvPostPrice;
        private TextView tvPostStatus;
        private ImageButton btnEdit;
        private ImageButton btnDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostTitle = itemView.findViewById(R.id.tv_post_title);
            tvPostDescription = itemView.findViewById(R.id.tv_post_description);
            tvPostPrice = itemView.findViewById(R.id.tv_post_price);
            tvPostStatus = itemView.findViewById(R.id.tv_post_status);
            btnEdit = itemView.findViewById(R.id.btn_edit_post);
            btnDelete = itemView.findViewById(R.id.btn_delete_post);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostClick(postList.get(position));
                }
            });

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostEdit(postList.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostDelete(postList.get(position));
                }
            });
        }

        public void bind(Post post) {
            tvPostTitle.setText(post.getTitle() != null ? post.getTitle() : "N/A");
            tvPostDescription.setText(post.getDescription() != null ? post.getDescription() : "N/A");
            tvPostPrice.setText(post.getPrice() != null ? "$" + post.getPrice() : "N/A");
            tvPostStatus.setText(post.getStatus() != null ? post.getStatus() : "N/A");
        }
    }
} 