package com.example.preely.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.request.CategoryFilterRequest;
import com.example.preely.model.request.SortFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SortFilterAdapter extends RecyclerView.Adapter<SortFilterAdapter.SortFilterViewHolder> {

    private final List<SortFilterRequest> itemList;

    public SortFilterAdapter(List<SortFilterRequest> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public SortFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_checkbox, parent, false);
        return new SortFilterAdapter.SortFilterViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull SortFilterViewHolder holder, int position) {
        SortFilterRequest item = itemList.get(position);
        holder.checkBox.setText(item.getName());
        holder.checkBox.setChecked(item.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (int i = 0; i < itemList.size(); i++) {
                    itemList.get(i).setChecked(false);
                }
                item.setChecked(true);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public static class SortFilterViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public SortFilterViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public Integer getSelectedItem() {
        for (SortFilterRequest item : itemList) {
            if (item.isChecked()) return item.getSortType();
        }
        return null;
    }
}
