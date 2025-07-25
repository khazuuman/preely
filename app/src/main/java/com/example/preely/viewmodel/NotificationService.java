package com.example.preely.viewmodel;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.preely.R;
import com.example.preely.model.entities.Message;
import com.example.preely.receiver.MessageNotificationReceiver;
import com.example.preely.view.ChatDetailActivity;
import com.example.preely.view.ChatListActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "message_notifications";
    private static final String CHANNEL_NAME = "Tin nhắn";
    private static final String CHANNEL_DESCRIPTION = "Thông báo tin nhắn mới";
    private static final int NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManagerCompat notificationManager;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    /**
     *  Tạo notification channel cho Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // High để hiển thị heads-up
            );

            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.setShowBadge(true); // Enable app badge
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            channel.enableLights(true);
            channel.setLightColor(android.graphics.Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            Log.d(TAG, "Notification channel created");
        }
    }

    /**
     * Hiển thị notification tin nhắn mới
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void showMessageNotification(Message message, String senderName, int unreadCount) {
        try {
            // Kiểm tra setting thông báo
            if (!isNotificationEnabled()) {
                Log.d(TAG, "Notifications disabled by user");
                return;
            }

            // Intent mở ChatDetailActivity
            Intent chatIntent = new Intent(context, ChatDetailActivity.class);
            chatIntent.putExtra("ROOM_ID", message.getRoom());
            chatIntent.putExtra("RECEIVER_ID", message.getSender_id().getId());
            chatIntent.putExtra("RECEIVER_NAME", senderName);
            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            chatIntent.setAction("OPEN_CHAT_" + System.currentTimeMillis()); // Unique action

            PendingIntent chatPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    chatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Intent mở ChatListActivity
            Intent listIntent = new Intent(context, ChatListActivity.class);
            listIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent listPendingIntent = PendingIntent.getActivity(
                    context,
                    1,
                    listIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Intent đánh dấu đã đọc
            Intent markReadIntent = new Intent(context, MessageNotificationReceiver.class);
            markReadIntent.setAction("MARK_AS_READ");
            markReadIntent.putExtra("MESSAGE_ID", message.getId());
            markReadIntent.putExtra("ROOM_ID", message.getRoom());
            markReadIntent.putExtra("SENDER_ID", message.getSender_id().getId());

            PendingIntent markReadPendingIntent = PendingIntent.getBroadcast(
                    context,
                    2,
                    markReadIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Intent reply (có thể implement sau)
            Intent replyIntent = new Intent(context, MessageNotificationReceiver.class);
            replyIntent.setAction("QUICK_REPLY");
            replyIntent.putExtra("ROOM_ID", message.getRoom());
            replyIntent.putExtra("SENDER_ID", message.getSender_id().getId());

            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    3,
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Tạo notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_message_notification)
                    .setContentTitle(senderName)
                    .setContentText(message.getContent())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message.getContent())
                            .setBigContentTitle(senderName)
                            .setSummaryText(unreadCount > 1 ? (unreadCount + " tin nhắn mới") : ""))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setContentIntent(chatPendingIntent) // Click vào notification
                    .setNumber(unreadCount) // Badge count
                    .setWhen(message.getSend_at().toDate().getTime())
                    .setShowWhen(true)
                    .setColor(context.getResources().getColor(R.color.red, null))
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setPublicVersion(createPublicNotification(senderName, unreadCount));

            // Thêm action buttons
            builder.addAction(R.drawable.ic_mark_read, "Đã đọc", markReadPendingIntent);
            builder.addAction(R.drawable.ic_chat, "Mở chat", listPendingIntent);

            // Notification cho lock screen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            }

            notificationManager.notify(NOTIFICATION_ID, builder.build());

            Log.d(TAG, "Message notification shown for: " + senderName + ", unread: " + unreadCount);

        } catch (Exception e) {
            Log.e(TAG, "Error showing message notification: " + e.getMessage());
        }
    }

    /**
     *  Tạo public notification cho lock screen
     */
    private android.app.Notification createPublicNotification(String senderName, int count) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message_notification)
                .setContentTitle("Tin nhắn mới")
                .setContentText(count > 1 ? count + " tin nhắn mới" : "1 tin nhắn mới từ " + senderName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }

    /**
     *  Kiểm tra setting thông báo
     */
    private boolean isNotificationEnabled() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        return prefs.getBoolean("notification_enabled", true);
    }

    /**
     *  Xóa notification
     */
    public void clearNotification() {
        try {
            notificationManager.cancel(NOTIFICATION_ID);
            Log.d(TAG, "Notification cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing notification: " + e.getMessage());
        }
    }

    /**
     *  Cập nhật badge count
     */
    public void updateBadgeCount(int count) {
        if (count <= 0) {
            clearNotification();
        }

        // Có thể thêm logic update app icon badge ở đây
        Log.d(TAG, "Badge count updated: " + count);
    }

    /**
     *  Kiểm tra permission notification
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return notificationManager.areNotificationsEnabled();
        }
        return true;
    }
}
