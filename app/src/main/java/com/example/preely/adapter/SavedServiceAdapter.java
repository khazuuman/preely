package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.preely.R;
import com.example.preely.model.entities.Service;
import com.example.preely.util.Constraints;
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
        
        // Set title
        holder.title.setText(service.getTitle());
        
        // Set price and price unit
        if (service.getPrice() != null) {
            holder.price.setText("$" + service.getPrice());
        } else {
            holder.price.setText("$0");
        }
        
        // Set price unit
        if (service.getPrice_unit() != null) {
            if (service.getPrice_unit().equals(Constraints.PriceUnitType.HOUR)) {
                holder.priceUnit.setText("/H");
            } else if (service.getPrice_unit().equals(Constraints.PriceUnitType.DAY)) {
                holder.priceUnit.setText("/D");
            } else if (service.getPrice_unit().equals(Constraints.PriceUnitType.WEEK)) {
                holder.priceUnit.setText("/W");
            } else if (service.getPrice_unit().equals(Constraints.PriceUnitType.MONTH)) {
                holder.priceUnit.setText("/M");
            } else if (service.getPrice_unit().equals(Constraints.PriceUnitType.ONCE)) {
                holder.priceUnit.setVisibility(View.GONE);
            }
        } else {
            holder.priceUnit.setVisibility(View.GONE);
        }
        
        // Set status
        if (service.getAvailability() != null) {
            holder.status.setText(service.getAvailability().getLabel());
        } else {
            holder.status.setText("Unknown");
        }
        
        // Set provider name
        String providerName = "Unknown";
        if (service.getProvider_id() != null) {
            String providerId = service.getProvider_id().getId();
            if (providerIdToName.containsKey(providerId)) {
                providerName = providerIdToName.get(providerId);
            }
        }
        holder.provider.setText(providerName);
        
        // Set category name
        String categoryName = "Unknown";
        if (service.getCategory_id() != null) {
            String categoryId = service.getCategory_id().getId();
            if (categoryIdToName.containsKey(categoryId)) {
                categoryName = categoryIdToName.get(categoryId);
            }
        }
        holder.category.setText(categoryName);
        
        // Set rating (default to 4.5 if not available)
        holder.rating.setText("4.5/5");
        
        // Load service image
        if (service.getImage_urls() != null && !service.getImage_urls().isEmpty()) {
            String imageUrl = service.getImage_urls().get(0);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.img_test)
                    .error(R.drawable.img_test)
                    .centerCrop()
                    .into(holder.serviceImage);
        } else {
            holder.serviceImage.setImageResource(R.drawable.img_test);
        }
        
        // Set click listeners
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
        TextView title, price, priceUnit, status, provider, category, rating;
        ImageView serviceImage;
        MaterialButton btnRemove;
        
        SavedServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_service_title);
            price = itemView.findViewById(R.id.tv_service_price);
            priceUnit = itemView.findViewById(R.id.tv_price_unit);
            status = itemView.findViewById(R.id.tv_service_status);
            provider = itemView.findViewById(R.id.tv_service_provider);
            category = itemView.findViewById(R.id.tv_service_category);
            rating = itemView.findViewById(R.id.tv_rating);
            serviceImage = itemView.findViewById(R.id.service_img);
            btnRemove = itemView.findViewById(R.id.btn_remove_saved);
        }
    }
} 