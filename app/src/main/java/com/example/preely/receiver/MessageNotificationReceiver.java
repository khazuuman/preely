package com.example.preely.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.preely.viewmodel.NotificationService;

public class MessageNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("MARK_AS_READ".equals(action)) {
            String messageId = intent.getStringExtra("MESSAGE_ID");
            String roomId = intent.getStringExtra("ROOM_ID");

            Log.d("NotificationReceiver", "Mark as read: " + messageId);

            // TODO: Implement mark as read logic
            // Có thể tạo một static method trong MessageService
            // hoặc sử dụng repository trực tiếp

            NotificationService notificationService = new NotificationService(context);
            notificationService.clearNotification();
        }
    }
}
