package com.example.preely.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.SavedPostRequest;
import com.example.preely.model.response.PostResponse;
import com.example.preely.view.PostDetailActivity;
import com.example.preely.viewmodel.PostService;

import java.util.List;

public class PostMarketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<PostResponse> postList;
    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;

    public PostMarketAdapter(List<PostResponse> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_market, parent, false);
            return new PostViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            PostResponse post = postList.get(position);
            PostViewHolder postHolder = (PostViewHolder) holder;
            postHolder.postTitle.setText(post.getTitle());
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                Glide.with(postHolder.postImg.getContext())
                        .load(post.getImages().get(0))
                        .placeholder(R.drawable.img_not_found)
                        .centerCrop()
                        .into(postHolder.postImg);
            } else {
                postHolder.postImg.setImageResource(R.drawable.img_not_found);
            }
            postHolder.postWard.setText(post.getWard());
            postHolder.postProvince.setText(post.getProvince());
            if (post.getCategoryResponse() != null) {
                postHolder.postCategory.setText(post.getCategoryResponse().getName());
            }
            postHolder.tagRecycleView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            postHolder.tagRecycleView.setAdapter(new TagPostAdapter(post.getTagResponses()));
            postHolder.favoriteBtn.setOnClickListener(v -> {
                SavedPostRequest request = new SavedPostRequest();
                SessionManager sessionManager = new SessionManager(holder.itemView.getContext());
                request.setPost_id(post.getId());
                request.setUser_id(sessionManager.getUserSession().getId());
                PostService postService = new PostService();
                try {
                    postService.insertSavedPost(request);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            });
            postHolder.postImg.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), PostDetailActivity.class);
                intent.putExtra("postId", post.getId().getId());
                holder.itemView.getContext().startActivity(intent);
            });
            postHolder.postTitle.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), PostDetailActivity.class);
                intent.putExtra("postId", post.getId().getId());
                holder.itemView.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return postList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle, postWard, postProvince, postCategory;
        RecyclerView tagRecycleView;
        ImageView postImg;
        Button favoriteBtn;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.post_title);
            postWard = itemView.findViewById(R.id.post_ward);
            postProvince = itemView.findViewById(R.id.post_province);
            postCategory = itemView.findViewById(R.id.post_category);
            tagRecycleView = itemView.findViewById(R.id.tag_recycle_view);
            favoriteBtn = itemView.findViewById(R.id.favoriteBtn);
            postImg = itemView.findViewById(R.id.post_img);
        }
    }

    private static class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
