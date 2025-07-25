package com.example.preely.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.preely.authentication.SessionManager;
import com.example.preely.viewmodel.MessageService;
import com.example.preely.viewmodel.NotificationService;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class MessageNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        try {
            if ("MARK_AS_READ".equals(action)) {
                handleMarkAsRead(context, intent);
            } else if ("QUICK_REPLY".equals(action)) {
                handleQuickReply(context, intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling notification action: " + e.getMessage());
        }
    }

    /**
     * ✅ Xử lý đánh dấu tin nhắn đã đọc
     */
    private void handleMarkAsRead(Context context, Intent intent) {
        String messageId = intent.getStringExtra("MESSAGE_ID");
        String roomId = intent.getStringExtra("ROOM_ID");
        String senderId = intent.getStringExtra("SENDER_ID");

        Log.d(TAG, "Mark as read - Room: " + roomId + ", Message: " + messageId);

        // Lấy current user từ session
        SessionManager sessionManager = new SessionManager(context);
        if (sessionManager.getUserSession() == null) {
            Log.e(TAG, "No user session for mark as read");
            return;
        }

        String currentUserId = sessionManager.getUserSession().getId();

        // ✅ Đánh dấu tin nhắn đã đọc trực tiếp với Firestore
        markMessagesAsReadDirect(context, roomId, currentUserId);
    }

    /**
     * ✅ Đánh dấu tin nhắn đã đọc trực tiếp với Firestore
     */
    private void markMessagesAsReadDirect(Context context, String roomId, String currentUserId) {
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection("user") // Sử dụng collection name từ code
                .document(currentUserId);

        // Query tin nhắn chưa đọc trong room này
        FirebaseFirestore.getInstance()
                .collection("message") // Collection name của Message
                .whereEqualTo("room", roomId)
                .whereEqualTo("receiver_id", currentUserRef)
                .whereEqualTo("_read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        WriteBatch batch = FirebaseFirestore.getInstance().batch();

                        // Batch update tất cả tin nhắn chưa đọc
                        querySnapshot.getDocuments().forEach(doc -> {
                            batch.update(doc.getReference(), "_read", true);
                        });

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Marked " + querySnapshot.size() + " messages as read");

                                    // Clear notification
                                    NotificationService notificationService = new NotificationService(context);
                                    notificationService.clearNotification();

                                    // Broadcast update để update UI
                                    broadcastUnreadCountUpdate(context, 0);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to mark messages as read: " + e.getMessage());
                                });
                    } else {
                        // Không có tin nhắn chưa đọc, chỉ clear notification
                        NotificationService notificationService = new NotificationService(context);
                        notificationService.clearNotification();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying unread messages: " + e.getMessage());
                });
    }

    /**
     * ✅ Xử lý quick reply (có thể implement sau)
     */
    private void handleQuickReply(Context context, Intent intent) {
        String roomId = intent.getStringExtra("ROOM_ID");
        String senderId = intent.getStringExtra("SENDER_ID");

        Log.d(TAG, "Quick reply - Room: " + roomId + ", Sender: " + senderId);

        // TODO: Implement quick reply functionality
        // Có thể mở dialog hoặc activity để nhập tin nhắn nhanh
    }

    /**
     * ✅ Broadcast cập nhật unread count
     */
    private void broadcastUnreadCountUpdate(Context context, int unreadCount) {
        Intent intent = new Intent("UPDATE_UNREAD_COUNT");
        intent.putExtra("unread_count", unreadCount);
        context.sendBroadcast(intent);

        Log.d(TAG, "Broadcast unread count update: " + unreadCount);
    }
}
