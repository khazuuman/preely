package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.model.entities.Service;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<Service> serviceList;
    private final OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onEdit(Service service);
        void onDelete(Service service);
        void onItemClick(Service service);
    }

    public ServiceAdapter(List<Service> serviceList, OnServiceClickListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.title.setText(service.getTitle());
        holder.description.setText(service.getDescription());
        holder.price.setText("$" + service.getPrice());
        holder.status.setText(service.getAvailability());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(service));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(service));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(service));
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, price, status, provider, category;
        ImageButton btnEdit, btnDelete;
        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_service_title);
            description = itemView.findViewById(R.id.tv_service_description);
            price = itemView.findViewById(R.id.tv_service_price);
            status = itemView.findViewById(R.id.tv_service_status);
            provider = itemView.findViewById(R.id.tv_service_provider);
            category = itemView.findViewById(R.id.tv_service_category);
            btnEdit = itemView.findViewById(R.id.btn_edit_service);
            btnDelete = itemView.findViewById(R.id.btn_delete_service);
        }
    }
} 