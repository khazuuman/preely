package com.example.preely.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.view.PostListActivity;

import java.util.List;

public class CategoryMarketAdapter extends RecyclerView.Adapter<CategoryMarketAdapter.CategoryViewHolder> {


    private final List<CategoryResponse> categoryList;

    public CategoryMarketAdapter(List<CategoryResponse> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_market, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryResponse category = categoryList.get(position);
        holder.cateName.setText(category.getName());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PostListActivity.class);
            intent.putExtra("category_id", category.getId().getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView cateName;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cateName = itemView.findViewById(R.id.cate_name);
        }
    }

}
