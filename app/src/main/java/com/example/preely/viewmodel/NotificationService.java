package com.example.preely.viewmodel;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.preely.R;
import com.example.preely.model.entities.Message;
import com.example.preely.receiver.MessageNotificationReceiver;
import com.example.preely.view.ChatDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationService {
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Cho phép hiển thị trên lock screen
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.setShowBadge(true); // Enable app badge
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void showMessageNotification(Message message, String senderName, int unreadCount) {
        // Kiểm tra trạng thái bật/tắt thông báo
        android.content.SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("notification_enabled", true);
        if (!enabled) return;

        Intent chatIntent = new Intent(context, ChatDetailActivity.class);
        chatIntent.putExtra("ROOM_ID", message.getRoom());
        chatIntent.putExtra("RECEIVER_ID", message.getSender_id().getId());
        chatIntent.putExtra("RECEIVER_NAME", senderName);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                chatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent markReadIntent = new Intent(context, MessageNotificationReceiver.class);
        markReadIntent.setAction("MARK_AS_READ");
        markReadIntent.putExtra("MESSAGE_ID", message.getId().getId());
        markReadIntent.putExtra("ROOM_ID", message.getRoom());

        PendingIntent markReadPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                markReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message_notification)
                .setContentTitle(senderName)
                .setContentText(message.getContent())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message.getContent()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true) // Tự động dismiss khi click
                .setContentIntent(pendingIntent)
                .setNumber(unreadCount) // Badge count
                .setWhen(message.getSend_at().toDate().getTime())
                .setShowWhen(true)
                .addAction(R.drawable.ic_mark_read, "Đánh dấu đã đọc", markReadPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(createPublicNotification(senderName, unreadCount));

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private android.app.Notification createPublicNotification(String senderName, int count) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message_notification)
                .setContentTitle("Tin nhắn mới")
                .setContentText(count > 1 ? count + " tin nhắn mới" : "1 tin nhắn mới")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    public void clearNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void updateBadgeCount(int count) {
        if (count <= 0) {
            clearNotification();
        }
    }
}
