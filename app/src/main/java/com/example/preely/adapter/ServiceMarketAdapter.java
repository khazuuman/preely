package com.example.preely.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.SavedServiceRequest;
import com.example.preely.model.response.ServiceMarketResponse;
import com.example.preely.util.Constraints;
import com.example.preely.view.CustomToast;
import com.example.preely.view.ServiceDetailActivity;
import com.example.preely.viewmodel.ServiceMarketViewModel;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.entities.Service;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Setter;

public class ServiceMarketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ServiceMarketResponse> serviceList;
    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private ServiceMarketViewModel serviceMarketViewModel;
    private LifecycleOwner lifecycleOwner;

    public ServiceMarketAdapter(List<ServiceMarketResponse> serviceList) {
        this.serviceList = serviceList;
    }

    public ServiceMarketAdapter(List<ServiceMarketResponse> serviceList, LifecycleOwner lifecycleOwner, ServiceMarketViewModel serviceMarketViewModel) {
        this.serviceList = serviceList;
        this.lifecycleOwner = lifecycleOwner;
        this.serviceMarketViewModel = serviceMarketViewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_market, parent, false);
            return new ServiceMarketViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingHolder(view);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ServiceMarketResponse response = serviceList.get(position);
            ServiceMarketViewHolder serviceHolder = (ServiceMarketViewHolder) holder;
            //title
            serviceHolder.serviceTitle.setText(response.getTitle());
            //image
            if (response.getImage() != null && !response.getImage().isEmpty()) {
                Glide.with(serviceHolder.serviceImg.getContext())
                        .load(response.getImage())
                        .placeholder(R.drawable.img_not_found)
                        .centerCrop()
                        .into(serviceHolder.serviceImg);
            } else {
                serviceHolder.serviceImg.setImageResource(R.drawable.img_not_found);
            }
            //provider
            serviceHolder.serviceProvider.setText(response.getProviderName());
            //category
            serviceHolder.serviceCategory.setText(response.getCategoryName());
            // rating
            if (response.getAverage_rating() != null) {
                serviceHolder.tvRating.setText(String.format("%.1f", response.getAverage_rating()));
            } else {
                serviceHolder.tvRating.setText("0");
            }
            //status
            serviceHolder.serviceStatus.setText(response.getStatus());
            //availability
            if (response.getAvailability() != null) {
                serviceHolder.tvAvailable.setText(response.getAvailability().getLabel());
            } else {
                serviceHolder.tvAvailable.setText("N/A");
            }
            //price
            serviceHolder.servicePrice.setText(NumberFormat.getNumberInstance(Locale.US).format(response.getPrice()) + " VND");
            //price unit
            if (response.getPrice_unit().equals(Constraints.PriceUnitType.HOUR)) {
                serviceHolder.priceUnit.setText("/H");
            } else if (response.getPrice_unit().equals(Constraints.PriceUnitType.DAY)) {
                serviceHolder.priceUnit.setText("/D");
            } else if (response.getPrice_unit().equals(Constraints.PriceUnitType.WEEK)) {
                serviceHolder.priceUnit.setText("/W");
            } else if (response.getPrice_unit().equals(Constraints.PriceUnitType.MONTH)) {
                serviceHolder.priceUnit.setText("/M");
            } else if (response.getPrice_unit().equals(Constraints.PriceUnitType.ONCE)) {
                serviceHolder.priceUnit.setVisibility(View.GONE);
            }
            //click event
            serviceHolder.serviceImg.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), ServiceDetailActivity.class);
                intent.putExtra("serviceId", response.getId());
                holder.itemView.getContext().startActivity(intent);
            });

            serviceHolder.skillRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            SkillMarketAdapter skillAdapter = new SkillMarketAdapter(response.getSkills());
            serviceHolder.skillRecyclerView.setAdapter(skillAdapter);

            SessionManager sessionManager = new SessionManager(holder.itemView.getContext());

            serviceHolder.favoriteBtn.setOnClickListener(v -> {
                SavedServiceRequest request = new SavedServiceRequest();
                DocumentReference serviceRef = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.SERVICE).document(response.getId());
                DocumentReference userRef = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.USERS).document(sessionManager.getUserSession().getId());
                request.setService_id(serviceRef);
                request.setUser_id(userRef);
                try {
                    serviceMarketViewModel.checkSavedPost(request);
                    observeOnce(serviceMarketViewModel.getIsSavedServiceExisted(), lifecycleOwner, isExisted -> {
                        if (isExisted) {
                            CustomToast.makeText(holder.itemView.getContext(), "Service already saved", CustomToast.LENGTH_SHORT, Constraints.NotificationType.SUCCESS).show();
                        } else {
                            CustomToast.makeText(holder.itemView.getContext(), "Save service successfully", CustomToast.LENGTH_SHORT, Constraints.NotificationType.SUCCESS).show();
                        }
                    });

                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static <T> void observeOnce(LiveData<T> liveData, LifecycleOwner owner, Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this);
            }
        });
    }


    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return serviceList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public static class ServiceMarketViewHolder extends RecyclerView.ViewHolder {

        ImageView serviceImg;
        TextView serviceTitle, serviceProvider, serviceCategory, servicePrice, serviceStatus, tvRating, tvAvailable, priceUnit;
        MaterialButton favoriteBtn;
        RecyclerView skillRecyclerView;

        public ServiceMarketViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImg = itemView.findViewById(R.id.service_img);
            serviceTitle = itemView.findViewById(R.id.tv_title);
            serviceProvider = itemView.findViewById(R.id.tv_provider);
            serviceCategory = itemView.findViewById(R.id.tv_category);
            servicePrice = itemView.findViewById(R.id.tv_price);
            serviceStatus = itemView.findViewById(R.id.tv_status);
            favoriteBtn = itemView.findViewById(R.id.saved_service_btn);
            skillRecyclerView = itemView.findViewById(R.id.skill_recycler_view);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvAvailable = itemView.findViewById(R.id.tv_available);
            priceUnit = itemView.findViewById(R.id.price_unit);
        }
    }

    private static class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price) + " VND";
    }
} 