package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.preely.model.response.ServiceMarketResponse;
import com.example.preely.viewmodel.ServiceMarketViewModel;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.entities.Service;

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ServiceMarketResponse response = serviceList.get(position);
            ServiceMarketViewHolder serviceHolder = (ServiceMarketViewHolder) holder;
            serviceHolder.serviceTitle.setText(response.getTitle());
            if (response.getImage() != null && !response.getImage().isEmpty()) {
                Glide.with(serviceHolder.serviceImg.getContext())
                        .load(response.getImage())
                        .placeholder(R.drawable.img_not_found)
                        .centerCrop()
                        .into(serviceHolder.serviceImg);
            } else {
                serviceHolder.serviceImg.setImageResource(R.drawable.img_not_found);
            }
            serviceHolder.serviceProvider.setText(response.getProviderName());
            serviceHolder.serviceCategory.setText(response.getCategoryName());
            serviceHolder.servicePrice.setText(formatPrice(response.getPrice()));
            serviceHolder.serviceStatus.setText(response.getStatus());

//            SessionManager sessionManager = new SessionManager(holder.itemView.getContext());

            serviceHolder.favoriteBtn.setOnClickListener(v -> {
//                SavedPostRequest request = new SavedPostRequest();
//                request.setPost_id(post.getId());
//                request.setUser_id(sessionManager.getUserSession().getId());
//                try {
//                    postService.insertSavedPost(request);
//                    postService.getPostExisted().observe(lifecycleOwner, isExisted -> {
//                        if (isExisted) {
//                            CustomToast.makeText(holder.itemView.getContext(), "Bài đăng đã được lưu", CustomToast.LENGTH_SHORT, Constraints.NotificationType.SUCCESS).show();
//                        } else {
//                            CustomToast.makeText(holder.itemView.getContext(), "Đã lưu bài đăng", CustomToast.LENGTH_SHORT, Constraints.NotificationType.SUCCESS).show();
//
//                        }
//                    });
//                } catch (IllegalAccessException | InstantiationException e) {
//                    throw new RuntimeException(e);
//                }
            });

//            String key = sessionManager.getUserSession().getId() + "_" + post.getId();
//            Boolean isSaved = savedPostsStatusMap.get(key);
//
//            if (isSaved != null && isSaved) {
//                postHolder.favoriteBtn.setImageResource(R.drawable.ic_favorite_full_color);
//            } else {
//                postHolder.favoriteBtn.setImageResource(R.drawable.ic_favorite);
//            }


//            postHolder.postImg.setOnClickListener(v -> {
//                Intent intent = new Intent(holder.itemView.getContext(), PostDetailActivity.class);
//                intent.putExtra("postId", post.getId().getId());
//                holder.itemView.getContext().startActivity(intent);
//            });
//            postHolder.postTitle.setOnClickListener(v -> {
//                Intent intent = new Intent(holder.itemView.getContext(), PostDetailActivity.class);
//                intent.putExtra("postId", post.getId().getId());
//                holder.itemView.getContext().startActivity(intent);
//            });
        }
    }

    @Setter
    private Map<String, Boolean> savedPostsStatusMap = new HashMap<>();


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
        TextView serviceTitle, serviceProvider, serviceCategory, servicePrice, serviceStatus;
        MaterialButton favoriteBtn;

        public ServiceMarketViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImg = itemView.findViewById(R.id.service_img);
            serviceTitle = itemView.findViewById(R.id.tv_service_title);
            serviceProvider = itemView.findViewById(R.id.tv_service_provider);
            serviceCategory = itemView.findViewById(R.id.tv_service_category);
            servicePrice = itemView.findViewById(R.id.tv_service_price);
            serviceStatus = itemView.findViewById(R.id.tv_service_status);
            favoriteBtn = itemView.findViewById(R.id.favorite_btn);
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