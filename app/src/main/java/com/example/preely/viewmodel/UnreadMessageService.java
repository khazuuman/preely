package com.example.preely.viewmodel;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.example.preely.authentication.SessionManager;
import com.example.preely.model.entities.Message;
import com.example.preely.util.Constraints;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UnreadMessageService extends Service {
    private static final String TAG = "UnreadMessageService";

    private FirestoreRealtimeUtil realtimeUtil;
    private NotificationService notificationService;
    private UserService userService;
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "UnreadMessageService created");

        realtimeUtil = new FirestoreRealtimeUtil();
        notificationService = new NotificationService(this);
        userService = new UserService();
        sessionManager = new SessionManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🚀 UnreadMessageService onStartCommand called");

        if (sessionManager.getUserSession() != null) {
            String userId = sessionManager.getUserSession().getId();
            Log.d(TAG, "✅ User session found: " + userId);
            setupRealtimeListener();
        } else {
            Log.e(TAG, "❌ No user session, stopping service");
            stopSelf();
        }

        return START_STICKY;
    }

    /**
     * ✅ Setup realtime listener cho tin nhắn mới
     */
    private void setupRealtimeListener() {
        try {
            String userId = sessionManager.getUserSession().getId();
            DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                    .collection("user") // ⚠️ Kiểm tra collection name
                    .document(userId);

            Log.d(TAG, "🎯 Setting up listener for user: " + userId);
            Log.d(TAG, "🎯 User reference path: " + currentUserRef.getPath());

            // Query tin nhắn chưa đọc
            Query newMessageQuery = FirebaseFirestore.getInstance()
                    .collection(Constraints.CollectionName.MESSAGES) // ⚠️ Kiểm tra collection name
                    .whereEqualTo("receiver_id", currentUserRef)
                    .whereEqualTo("_read", false)
                    .orderBy("send_at", Query.Direction.DESCENDING);

            Log.d(TAG, "🔍 Query setup - Collection: " + Constraints.CollectionName.MESSAGES);

            realtimeUtil.listenToCustomQuery(newMessageQuery, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
                @Override
                public void onDataAdded(Message message) {
                    Log.d(TAG, "🆕 NEW MESSAGE DETECTED!");
                    Log.d(TAG, "   Content: " + message.getContent());
                    Log.d(TAG, "   Sender: " + (message.getSender_id() != null ? message.getSender_id().getId() : "null"));
                    Log.d(TAG, "   Room: " + message.getRoom());
                    Log.d(TAG, "   Is Read: " + message.is_read());

                    handleNewMessage(message, currentUserRef);
                }

                @Override
                public void onDataModified(Message message) {
                    Log.d(TAG, "📝 Message modified: " + message.getId());
                    updateUnreadCount(currentUserRef);
                }

                @Override
                public void onDataRemoved(Message message) {
                    Log.d(TAG, "🗑️ Message removed: " + message.getId());
                    updateUnreadCount(currentUserRef);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Realtime listener error: " + error);
                }
            });

            Log.d(TAG, "✅ Realtime listener setup completed");

        } catch (Exception e) {
            Log.e(TAG, "💥 Error setting up realtime listener: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Xử lý tin nhắn mới
     */
    private void handleNewMessage(Message message, DocumentReference currentUserRef) {
        if (message.getSender_id() == null) return;

        String senderId = message.getSender_id().getId();

        // Lấy tên người gửi
        userService.getUserInfo(senderId, new UserService.UserInfoCallback() {
            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            @Override
            public void onSuccess(String fullName, String username) {
                String senderName = fullName != null && !fullName.equals("Unknown Name")
                        ? fullName : username;

                // Đếm tổng số tin nhắn chưa đọc
                countUnreadMessages(currentUserRef, unreadCount -> {
                    // Hiển thị notification
                    if (notificationService.hasNotificationPermission()) {
                        notificationService.showMessageNotification(message, senderName, unreadCount);
                    }

                    // Broadcast update unread count
                    broadcastUnreadCountUpdate(unreadCount);
                });
            }

            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to get sender info: " + error);

                // Fallback với tên mặc định
                countUnreadMessages(currentUserRef, unreadCount -> {
                    if (notificationService.hasNotificationPermission()) {
                        notificationService.showMessageNotification(message, "Người dùng", unreadCount);
                    }
                    broadcastUnreadCountUpdate(unreadCount);
                });
            }
        });

        Log.d(TAG, "New message handled from: " + senderId);
    }

    /**
     * ✅ Cập nhật unread count
     */
    private void updateUnreadCount(DocumentReference currentUserRef) {
        countUnreadMessages(currentUserRef, this::broadcastUnreadCountUpdate);
    }

    /**
     *  Đếm tin nhắn chưa đọc
     */
    private void countUnreadMessages(DocumentReference currentUserRef, UnreadCountCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("receiver_id", currentUserRef)
                .whereEqualTo("_read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    callback.onCount(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error counting unread messages: " + e.getMessage());
                    callback.onCount(0);
                });
    }

    /**
     * ✅ Broadcast unread count update
     */
    private void broadcastUnreadCountUpdate(int unreadCount) {
        Intent intent = new Intent("UPDATE_UNREAD_COUNT");
        intent.putExtra("unread_count", unreadCount);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realtimeUtil != null) {
            realtimeUtil.removeAllListeners();
        }
        Log.d(TAG, "UnreadMessageService destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Interface cho callback
    private interface UnreadCountCallback {
        void onCount(int count);
    }
}
