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

public class ServiceMarketAdapter extends RecyclerView.Adapter<ServiceMarketAdapter.ServiceMarketViewHolder> {
    private List<Service> serviceList;
    private final OnServiceMarketClickListener listener;

    public interface OnServiceMarketClickListener {
        void onBook(Service service);
        void onItemClick(Service service);
    }

    public ServiceMarketAdapter(List<Service> serviceList, OnServiceMarketClickListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceMarketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_market, parent, false);
        return new ServiceMarketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceMarketViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.title.setText(service.getTitle());
        holder.price.setText("$" + service.getPrice());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(service));
        holder.btnBook.setOnClickListener(v -> listener.onBook(service));
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
        notifyDataSetChanged();
    }

    static class ServiceMarketViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, status, provider, category;
        MaterialButton btnBook;
        ServiceMarketViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_service_title);
            price = itemView.findViewById(R.id.tv_service_price);
            status = itemView.findViewById(R.id.tv_service_status);
            provider = itemView.findViewById(R.id.tv_service_provider);
            category = itemView.findViewById(R.id.tv_service_category);
            btnBook = itemView.findViewById(R.id.btn_book_service);
        }
    }
} 