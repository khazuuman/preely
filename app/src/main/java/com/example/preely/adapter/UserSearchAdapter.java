package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.response.UserResponse;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private List<UserResponse> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserResponse user);
    }

    public UserSearchAdapter(List<UserResponse> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = users.get(position);

        // Display user name - prioritize full name
        String displayName = user.getFull_name();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getUsername();
        }

        holder.userName.setText(displayName);

        // Display username as secondary information
        holder.userInfo.setText("@" + user.getUsername());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public void updateData(List<UserResponse> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userInfo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userInfo = itemView.findViewById(R.id.user_info);
        }
    }
}
