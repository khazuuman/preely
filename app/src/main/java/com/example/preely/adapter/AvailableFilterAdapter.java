package com.example.preely.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.request.AvailableFilterRequest;

import java.util.ArrayList;
import java.util.List;

public class AvailableFilterAdapter extends RecyclerView.Adapter<AvailableFilterAdapter.AvaiableViewHolder> {

    private final List<AvailableFilterRequest> itemList;

    public AvailableFilterAdapter(List<AvailableFilterRequest> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public AvaiableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_checkbox, parent, false);
        return new AvailableFilterAdapter.AvaiableViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull AvaiableViewHolder holder, int position) {
        AvailableFilterRequest item = itemList.get(position);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setText(item.getName());
        holder.checkBox.setChecked(item.isChecked());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (AvailableFilterRequest filter : itemList) {
                    filter.setChecked(false);
                }
                item.setChecked(true);
            } else {
                item.setChecked(false);
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public static class AvaiableViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public AvaiableViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public List<String> getSelectedItem() {
        List<String> enumNames = new ArrayList<>();
        for (AvailableFilterRequest item : itemList) {
            if (item.isChecked()) {
                enumNames.add(item.getEnumName());
            }
        }
        return enumNames;
    }

}
