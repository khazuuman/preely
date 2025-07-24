package com.example.preely.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.model.entities.Message;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ChatMessageAdapter";

    // View Types
    private static final int TYPE_MESSAGE_SENT = 1;
    private static final int TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> messageList;
    private String currentUserId;
    private SimpleDateFormat timeFormat;

    public ChatMessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // ✅ Log để debug
        Log.d(TAG, "ChatMessageAdapter created with currentUserId: " + currentUserId);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Message message = messageList.get(position);

            // ✅ Null checks để tránh crash
            if (message == null) {
                Log.w(TAG, "Message at position " + position + " is null");
                return TYPE_MESSAGE_RECEIVED; // Default fallback
            }

            if (message.getSender_id() == null) {
                Log.w(TAG, "Sender ID is null for message at position " + position);
                return TYPE_MESSAGE_RECEIVED; // Default fallback
            }

            if (currentUserId == null || currentUserId.isEmpty()) {
                Log.w(TAG, "Current user ID is null or empty");
                return TYPE_MESSAGE_RECEIVED; // Default fallback
            }

            // ✅ Safe comparison - sử dụng Objects.equals() hoặc manual null check
            String senderId = message.getSender_id().getId();

            if (senderId != null && senderId.equals(currentUserId)) {
                return TYPE_MESSAGE_SENT;
            } else {
                return TYPE_MESSAGE_RECEIVED;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in getItemViewType at position " + position + ": " + e.getMessage());
            return TYPE_MESSAGE_RECEIVED; // Safe fallback
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            Message message = messageList.get(position);

            // ✅ Null check
            if (message == null) {
                Log.w(TAG, "Cannot bind null message at position " + position);
                return;
            }

            if (holder instanceof SentMessageViewHolder) {
                ((SentMessageViewHolder) holder).bind(message);
            } else if (holder instanceof ReceivedMessageViewHolder) {
                ((ReceivedMessageViewHolder) holder).bind(message);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding message at position " + position + ": " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    // ✅ Method để update currentUserId nếu cần
    public void updateCurrentUserId(String newCurrentUserId) {
        if (newCurrentUserId != null && !newCurrentUserId.equals(this.currentUserId)) {
            this.currentUserId = newCurrentUserId;
            notifyDataSetChanged();
            Log.d(TAG, "Current user ID updated to: " + newCurrentUserId);
        }
    }

    // ✅ Method để update message list safely
    public void updateMessages(List<Message> newMessages) {
        if (newMessages != null) {
            this.messageList.clear();
            this.messageList.addAll(newMessages);
            notifyDataSetChanged();
            Log.d(TAG, "Messages updated. Count: " + newMessages.size());
        }
    }

    // ViewHolder cho tin nhắn đã gửi
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
        }

        void bind(Message message) {
            try {
                // Set message content
                if (message.getContent() != null) {
                    messageText.setText(message.getContent());
                } else {
                    messageText.setText(""); // Empty message
                }

                // Set time
                if (message.getSend_at() != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    timeText.setText(timeFormat.format(message.getSend_at().toDate()));
                } else {
                    timeText.setText("");
                }

            } catch (Exception e) {
                Log.e("SentMessageViewHolder", "Error binding message: " + e.getMessage());
                messageText.setText("Error loading message");
                timeText.setText("");
            }
        }
    }

    // ViewHolder cho tin nhắn nhận được
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
        }

        void bind(Message message) {
            try {
                // Set message content
                if (message.getContent() != null) {
                    messageText.setText(message.getContent());
                } else {
                    messageText.setText(""); // Empty message
                }

                // Set time
                if (message.getSend_at() != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    timeText.setText(timeFormat.format(message.getSend_at().toDate()));
                } else {
                    timeText.setText("");
                }

            } catch (Exception e) {
                Log.e("ReceivedMessageViewHolder", "Error binding message: " + e.getMessage());
                messageText.setText("Error loading message");
                timeText.setText("");
            }
        }
    }
}
