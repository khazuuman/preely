package com.example.preely.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.response.ChatRoomResponse;
import com.example.preely.view.ChatDetailActivity; // Thêm import này

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {
    private List<ChatRoomResponse> chatRooms;
    private Context context;

    public ChatRoomAdapter(List<ChatRoomResponse> chatRooms, Context context) {
        this.chatRooms = chatRooms;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoomResponse room = chatRooms.get(position);

        // Null check để tránh crash
        if (room == null) return;

        holder.receiverFullName.setText(room.getReceiverFullName() != null ? room.getReceiverFullName() : "Unknown");
        holder.receiverUsername.setText(room.getReceiverUsername() != null ? "@" + room.getReceiverUsername() : "@unknown");
        holder.lastMessage.setText(room.getLastMessage() != null ? room.getLastMessage() : "No message");

        // Click listener với error handling
        holder.itemView.setOnClickListener(v -> {
            try {
                // Kiểm tra data trước khi start activity
                if (room.getRoomId() != null && room.getReceiverId() != null) {
                    Intent intent = new Intent(context, ChatDetailActivity.class);
                    intent.putExtra("ROOM_ID", room.getRoomId());
                    intent.putExtra("RECEIVER_ID", room.getReceiverId());
                    intent.putExtra("RECEIVER_FULL_NAME", room.getReceiverFullName() != null ? room.getReceiverFullName() : "Unknown");
                    intent.putExtra("RECEIVER_USERNAME", room.getReceiverUsername() != null ? room.getReceiverUsername() : "Unknown");
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms != null ? chatRooms.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView receiverFullName, receiverUsername, lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverFullName = itemView.findViewById(R.id.receiver_full_name);
            receiverUsername = itemView.findViewById(R.id.receiver_username);
            lastMessage = itemView.findViewById(R.id.last_message);
        }
    }
}