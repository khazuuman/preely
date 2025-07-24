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

    private FirestoreRealtimeUtil realtimeUtil;
    private NotificationService notificationService;
    private UserService userService;
    private SessionManager sessionManager;
    private String currentUserId;

    @Override
    public void onCreate() {
        super.onCreate();

        realtimeUtil = new FirestoreRealtimeUtil(this);
        notificationService = new NotificationService(this);
        userService = new UserService();
        sessionManager = new SessionManager(this);

        if (sessionManager.getUserSession() != null) {
            currentUserId = sessionManager.getUserSession().getId();
            startListeningForNewMessages();
        }
    }

    private void startListeningForNewMessages() {
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection("user")
                .document(currentUserId);

        Query newMessageQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("receiver_id", currentUserRef)
                .whereEqualTo("is_read", false); // Chỉ tin nhắn chưa đọc

        realtimeUtil.listenToCustomQuery(newMessageQuery, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
            @Override
            public void onDataAdded(Message data) {
                handleNewUnreadMessage(data);
            }

            @Override
            public void onDataModified(Message data) {
                if (data.is_read()) {
                    updateUnreadCount();
                }
            }

            @Override
            public void onDataRemoved(Message data) {
                updateUnreadCount();
            }

            @Override
            public void onError(String error) {
                Log.e("UnreadMessageService", "Error listening for unread messages: " + error);
            }
        });
    }

    private void handleNewUnreadMessage(Message message) {
        Log.d("UnreadMessageService", "New unread message received from: " + message.getSender_id().getId());

        userService.getUserByRef(message.getSender_id(), new UserService.UserNameCallback() {
            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            @Override
            public void onSuccess(String userName) {
                countUnreadMessages(unreadCount -> {
                    notificationService.showMessageNotification(message, userName, unreadCount);
                    Intent updateIntent = new Intent("UPDATE_UNREAD_COUNT");
                    updateIntent.putExtra("unread_count", unreadCount);
                    sendBroadcast(updateIntent);
                });
            }

            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            @Override
            public void onFailure(String error) {
                Log.e("UnreadMessageService", "Failed to fetch sender name: " + error);
                countUnreadMessages(unreadCount -> {
                    notificationService.showMessageNotification(message, "Người dùng", unreadCount);
                });
            }
        });
    }

    private void updateUnreadCount() {
        countUnreadMessages(unreadCount -> {
            notificationService.updateBadgeCount(unreadCount);

            Intent updateIntent = new Intent("UPDATE_UNREAD_COUNT");
            updateIntent.putExtra("unread_count", unreadCount);
            sendBroadcast(updateIntent);
        });
    }

    public interface UnreadCountCallback {
        void onCountReceived(int count);
    }

    private void countUnreadMessages(UnreadCountCallback callback) {
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection("user")
                .document(currentUserId);

        Query unreadQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("receiver_id", currentUserRef)
                .whereEqualTo("is_read", false);

        unreadQuery.get().addOnSuccessListener(querySnapshot -> {
            int count = querySnapshot.size();
            Log.d("UnreadMessageService", "Unread messages count: " + count);
            callback.onCountReceived(count);
        }).addOnFailureListener(e -> {
            Log.e("UnreadMessageService", "Failed to count unread messages", e);
            callback.onCountReceived(0);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realtimeUtil != null) {
            realtimeUtil.removeAllListeners();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
