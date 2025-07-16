package com.example.preely.util;

import android.util.Log;

import com.example.preely.model.entities.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RealtimeDbUtil {
    private static final String TAG = "RealtimeDbUtil";
    private DatabaseReference dbRef;

    public RealtimeDbUtil() {
        dbRef = FirebaseDatabase.getInstance().getReference("messages");
    }

    public void sendMessageToRoom(String room, Message message) {
        DatabaseReference roomRef = dbRef.child(room).push();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender_id", message.getSender_id());
        messageMap.put("receiver_id", message.getReceiver_id());
        messageMap.put("content", message.getContent());
        messageMap.put("is_read", message.is_read());
        messageMap.put("send_at", message.getSend_at().toString());
        roomRef.setValue(messageMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Message sent"))
                .addOnFailureListener(e -> Log.e(TAG, "Send failed", e));
    }

    public void listenForMessages(String room, MessageListener listener) {
        dbRef.child(room).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message msg = snapshot.getValue(Message.class);
                if (msg != null) {
                    listener.onNewMessage(msg);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Listen cancelled", error.toException());
            }
        });
    }

    // Interface callback cho listener
    public interface MessageListener {
        void onNewMessage(Message message);
    }
}
