package com.example.preely.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.example.preely.R;
import com.example.preely.view.ChatActivity;
import com.example.preely.view.CustomToast;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationUtil {
    private static final String CHANNEL_ID = "preely_channel";
    private static final String CHANNEL_NAME = "Preely Notifications";
    private static final int NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, String title, String content, String receiverId) {
        // Check permission before proceeding
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted; handle gracefully (e.g., log or skip)
                return;
            }
        }

        createNotificationChannel(context);

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("RECEIVER_ID", receiverId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_person)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e("NotificationUtils", "SecurityException while trying to show notification: " + e.getMessage(), e);
            // Explicitly handle SecurityException as warned
            // Log the error or show a toast (using your CustomToast if available)
            showErrorToast(context, "Không thể hiển thị thông báo do thiếu quyền");
        }
    }

    public static void applyDotBadge(Context context, int unreadCount) {
        if (context == null) return;
        ShortcutBadger.applyCount(context, unreadCount);
    }

    private static void showErrorToast(Context context, String errorMessage) {
        if (context == null) return;  // Null check để an toàn
        CustomToast.makeText(context, errorMessage, Toast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
    }
}
