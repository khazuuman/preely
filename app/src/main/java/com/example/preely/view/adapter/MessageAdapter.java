package com.example.preely.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.entities.Message;

public class MessageAdapter extends ListAdapter<Message, MessageAdapter.ViewHolder> {
    private String currentUserId;

    public MessageAdapter() {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = getItem(position);
        holder.messageContent.setText(message.getContent());

        if (message.getSender_id() != null && message.getSender_id().equals(currentUserId)) {
            holder.messageContent.setBackgroundResource(R.drawable.bubble_right);  // Tận dụng drawable nếu có
            holder.messageContent.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            holder.messageContent.setBackgroundResource(R.drawable.bubble_left);  // Tận dụng drawable nếu có
            holder.messageContent.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.text_view_message_content);
        }
    }
}
