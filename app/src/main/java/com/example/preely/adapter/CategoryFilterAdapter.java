package com.example.preely.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.request.CategoryFilterRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class CategoryFilterAdapter extends RecyclerView.Adapter<CategoryFilterAdapter.CategoryFilterViewHolder> {
    @Setter
    private RecyclerView recyclerView;
    private final List<CategoryFilterRequest> itemList;

    public CategoryFilterAdapter(List<CategoryFilterRequest> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public CategoryFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_checkbox, parent, false);
        return new CategoryFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryFilterViewHolder holder, int position) {
        CategoryFilterRequest item = itemList.get(position);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setText(item.getName());
        holder.checkBox.setChecked(item.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            if ("All".equalsIgnoreCase(item.getName()) && isChecked) {
                item.setChecked(true);
                for (CategoryFilterRequest otherItem : itemList) {
                    if (!"All".equalsIgnoreCase(otherItem.getName())) {
                        otherItem.setChecked(false);
                    }
                }
            } else if (!"All".equalsIgnoreCase(item.getName()) && isChecked) {
                item.setChecked(true);
                for (CategoryFilterRequest otherItem : itemList) {
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

    public static class CategoryFilterViewHolder extends RecyclerView.ViewHolder {
        AppCompatCheckBox checkBox;

        public CategoryFilterViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public List<DocumentReference> getIdSelectedItems() {
        List<DocumentReference> selected = new ArrayList<>();
        for (CategoryFilterRequest item : itemList) {
            if (item.getId() == null && item.isChecked()) {
                return null;
            }
            if (item.isChecked()) {
                DocumentReference cateRef = FirebaseFirestore.getInstance().collection("category").document(item.getId());
                selected.add(cateRef);
            }
        }
        return selected;
    }
}
