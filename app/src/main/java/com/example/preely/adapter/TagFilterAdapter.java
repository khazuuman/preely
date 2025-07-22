package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.request.CategoryFilterRequest;
import com.example.preely.model.request.TagFilterRequest;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class TagFilterAdapter extends RecyclerView.Adapter<TagFilterAdapter.TagFilterViewHolder> {
    @Setter
    private RecyclerView recyclerView;
    private final List<TagFilterRequest> itemList;

    public TagFilterAdapter(List<TagFilterRequest> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public TagFilterAdapter.TagFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_checkbox, parent, false);
        return new TagFilterAdapter.TagFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagFilterAdapter.TagFilterViewHolder holder, int position) {
        TagFilterRequest item = itemList.get(position);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setText(item.getName());
        holder.checkBox.setChecked(item.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            if ("All".equalsIgnoreCase(item.getName()) && isChecked) {
                item.setChecked(true);
                for (TagFilterRequest otherItem : itemList) {
                    if (!"All".equalsIgnoreCase(otherItem.getName())) {
                        otherItem.setChecked(false);
                    }
                }
            } else if (!"All".equalsIgnoreCase(item.getName()) && isChecked) {
                item.setChecked(true);
                for (TagFilterRequest otherItem : itemList) {
                    if ("All".equalsIgnoreCase(otherItem.getName())) {
                        otherItem.setChecked(false);
                        break;
                    }
                }
            }
            if (recyclerView != null) {
                recyclerView.post(this::notifyDataSetChanged);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public static class TagFilterViewHolder extends RecyclerView.ViewHolder {
        AppCompatCheckBox checkBox;

        public TagFilterViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public List<String> getIdStringSelectedItems() {
        List<String> selected = new ArrayList<>();
        for (TagFilterRequest item : itemList) {
            if (item.getId() == null && item.isChecked()) {
                return null;
            }
            if (item.isChecked()) selected.add(item.getId().getId());
        }
        return selected;
    }
}