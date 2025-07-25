package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.SettingItem;
import androidx.appcompat.widget.SwitchCompat;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SWITCH = 1;
    private static final int TYPE_LOGOUT = 2;
    
    private List<SettingItem> settingsList;
    private OnSettingItemClickListener listener;
    
    public interface OnSettingItemClickListener {
        void onSettingItemClick(int position, SettingItem item);
        void onSwitchChanged(int position, SettingItem item, boolean isChecked);
    }
    
    public SettingsAdapter(List<SettingItem> settingsList, OnSettingItemClickListener listener) {
        this.settingsList = settingsList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case TYPE_SWITCH:
                View switchView = inflater.inflate(R.layout.item_setting_switch, parent, false);
                return new SwitchViewHolder(switchView);
            case TYPE_LOGOUT:
                View logoutView = inflater.inflate(R.layout.item_setting_logout, parent, false);
                return new LogoutViewHolder(logoutView);
            default:
                View normalView = inflater.inflate(R.layout.item_setting_normal, parent, false);
                return new NormalViewHolder(normalView);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem item = settingsList.get(position);
        
        switch (holder.getItemViewType()) {
            case TYPE_SWITCH:
                ((SwitchViewHolder) holder).bind(item, position);
                break;
            case TYPE_LOGOUT:
                ((LogoutViewHolder) holder).bind(item, position);
                break;
            default:
                ((NormalViewHolder) holder).bind(item, position);
                break;
        }
    }
    
    @Override
    public int getItemCount() {
        return settingsList.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        return settingsList.get(position).getItemType();
    }
    
    // Normal ViewHolder
    class NormalViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;
        private TextView subtitle;
        private ImageView arrow;
        
        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            title = itemView.findViewById(R.id.tvTitle);
            subtitle = itemView.findViewById(R.id.tvSubtitle);
            arrow = itemView.findViewById(R.id.ivArrow);
        }
        
        public void bind(SettingItem item, int position) {
            icon.setImageResource(item.getIconResId());
            title.setText(item.getTitle());
            
            if (item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(item.getSubtitle());
            } else {
                subtitle.setVisibility(View.GONE);
            }
            
            if (item.hasArrow()) {
                arrow.setVisibility(View.VISIBLE);
            } else {
                arrow.setVisibility(View.GONE);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSettingItemClick(position, item);
                }
            });
        }
    }
    
    // Switch ViewHolder
    class SwitchViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;
        private SwitchCompat switchView;
        
        public SwitchViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            title = itemView.findViewById(R.id.tvTitle);
            switchView = itemView.findViewById(R.id.switchView);
        }
        
        public void bind(SettingItem item, int position) {
            icon.setImageResource(item.getIconResId());
            title.setText(item.getTitle());
            switchView.setChecked(item.getSwitchState());
            
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSwitchChanged(position, item, isChecked);
                }
            });
        }
    }
    
    // Logout ViewHolder
    class LogoutViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;
        
        public LogoutViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            title = itemView.findViewById(R.id.tvTitle);
        }
        
        public void bind(SettingItem item, int position) {
            icon.setImageResource(item.getIconResId());
            title.setText(item.getTitle());
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSettingItemClick(position, item);
                }
            });
        }
    }
    
    public void updateSettingsList(List<SettingItem> newList) {
        this.settingsList = newList;
        notifyDataSetChanged();
    }
    
    public void updateSwitchState(int position, boolean isChecked) {
        if (position >= 0 && position < settingsList.size()) {
            settingsList.get(position).setSwitchState(isChecked);
            notifyItemChanged(position);
        }
    }
} 