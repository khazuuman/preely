package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.response.TagResponse;

import java.util.List;

public class TagPostAdapter extends RecyclerView.Adapter<TagPostAdapter.TagPostViewHolder>{

    private final List<TagResponse> tagList;

    public TagPostAdapter(List<TagResponse> tagList) {
        this.tagList = tagList;
    }

    @NonNull
    @Override
    public TagPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_in_post, parent, false);
        return new TagPostAdapter.TagPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagPostViewHolder holder, int position) {
        TagResponse tagResponse = tagList.get(position);
        holder.tagName.setText(tagResponse.getName());
    }

    @Override
    public int getItemCount() {
        return tagList == null ? 0 : tagList.size();
    }

    public static class TagPostViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        public TagPostViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tagName);
        }
    }

}
