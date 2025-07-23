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

public class SavedServiceAdapter extends RecyclerView.Adapter<SavedServiceAdapter.SavedServiceViewHolder> {
    private List<Service> serviceList;
    private final OnSavedServiceClickListener listener;

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
        holder.status.setText(service.getAvailability());
        holder.provider.setText("Provider: " + (service.getProvider_id() != null ? service.getProvider_id().getId() : "Unknown"));
        holder.category.setText("Category: " + (service.getCategory_id() != null ? service.getCategory_id().getId() : "Unknown"));
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