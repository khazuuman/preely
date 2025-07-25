package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.model.entities.Service;
import java.util.List;
import java.util.Map;

public class SavedServiceAdapter extends RecyclerView.Adapter<SavedServiceAdapter.SavedServiceViewHolder> {
    private List<Service> serviceList;
    private final OnSavedServiceClickListener listener;
    private Map<String, String> providerIdToName = new java.util.HashMap<>();
    private Map<String, String> categoryIdToName = new java.util.HashMap<>();

    public interface OnSavedServiceClickListener {
        void onRemove(Service service);
        void onItemClick(Service service);
    }

    public SavedServiceAdapter(List<Service> serviceList, OnSavedServiceClickListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavedServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_service, parent, false);
        return new SavedServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.title.setText(service.getTitle());
        holder.price.setText("$" + service.getPrice());
        holder.status.setText(service.getAvailability() != null ? service.getAvailability().getLabel() : "");
        String providerName = null;
        if (service.getProvider_id() != null) {
            providerName = providerIdToName.get(service.getProvider_id().getId());
        }
        holder.provider.setText("Provider: " + (providerName != null ? providerName : "Unknown"));
        String categoryName = null;
        if (service.getCategory_id() != null) {
            categoryName = categoryIdToName.get(service.getCategory_id().getId());
        }
        holder.category.setText("Category: " + (categoryName != null ? categoryName : "Unknown"));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(service));
        holder.btnRemove.setOnClickListener(v -> listener.onRemove(service));
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
        notifyDataSetChanged();
    }

    public void setProviderIdToName(Map<String, String> map) {
        this.providerIdToName = map;
        notifyDataSetChanged();
    }
    public void setCategoryIdToName(Map<String, String> map) {
        this.categoryIdToName = map;
        notifyDataSetChanged();
    }

    static class SavedServiceViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, status, provider, category;
        MaterialButton btnRemove;
        SavedServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_service_title);
            price = itemView.findViewById(R.id.tv_service_price);
            status = itemView.findViewById(R.id.tv_service_status);
            provider = itemView.findViewById(R.id.tv_service_provider);
            category = itemView.findViewById(R.id.tv_service_category);
            btnRemove = itemView.findViewById(R.id.btn_remove_saved);
        }
    }
} 