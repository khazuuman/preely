package com.example.preely.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.ChatRoomAdapter;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.entities.Message;
import com.example.preely.model.response.ChatRoomResponse;
import com.example.preely.util.Constraints;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.example.preely.viewmodel.MessageService;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private MessageService messageService;
    private ChatRoomAdapter adapter;
    private List<ChatRoomResponse> chatRoomList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private SessionManager sessionManager;
    private FirestoreRealtimeUtil realtimeUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        sessionManager = new SessionManager(this);
        realtimeUtil = new FirestoreRealtimeUtil();

        recyclerView = findViewById(R.id.chat_room_recycler_view);
        emptyTextView = findViewById(R.id.empty_chat_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatRoomAdapter(chatRoomList, this);
        recyclerView.setAdapter(adapter);

        messageService = new ViewModelProvider(this).get(MessageService.class);
        observeChatRooms();

        if (sessionManager.getUserSession() == null || sessionManager.getUserSession().getId() == null) {
            Log.e("ChatListActivity", "Session or user ID null");
            emptyTextView.setVisibility(View.VISIBLE);
            return;
        }

        String userId = sessionManager.getUserSession().getId();
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection("user")
                .document(userId);
        Log.d("DEBUG", "UserID for query: " + currentUserRef.getId());
        messageService.getChatRooms(currentUserRef);

        attachRealtimeListener(currentUserRef);
    }

    // Trong observeChatRooms method
    private void observeChatRooms() {
        messageService.getChatRoomsLiveData().observe(this, rooms -> {
            if (rooms != null && !rooms.isEmpty()) {
                Log.d("DEBUG", "=== Chat Rooms Debug ===");
                for (ChatRoomResponse room : rooms) {
                    Log.d("DEBUG", "Room ID: " + room.getRoomId());
                    Log.d("DEBUG", "Receiver ID: " + room.getReceiverId());
                    Log.d("DEBUG", "Receiver Name: " + room.getReceiverName());
                    Log.d("DEBUG", "Last Message: " + room.getLastMessage());
                    Log.d("DEBUG", "------------------------");
                }

                chatRoomList.clear();
                chatRoomList.addAll(rooms);
                adapter.notifyDataSetChanged();
                emptyTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Log.d("DEBUG", "Chat rooms loaded: " + rooms.size());
            } else {
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                CustomToast.makeText(this, "Không có cuộc trò chuyện nào", CustomToast.LENGTH_SHORT, Constraints.NotificationType.INFO).show();
                Log.d("DEBUG", "No chat rooms found");
            }
        });
    }

    private void attachRealtimeListener(DocumentReference currentUserRef) {
        Query senderQuery = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("sender_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        realtimeUtil.listenToCustomQuery(senderQuery, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
            @Override
            public void onDataAdded(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("DEBUG", "New message added (as sender)");
            }

            @Override
            public void onDataModified(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("DEBUG", "Message modified (as sender)");
            }

            @Override
            public void onDataRemoved(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("DEBUG", "Message removed (as sender)");
            }

            @Override
            public void onError(String error) {
                CustomToast.makeText(ChatListActivity.this, "Lỗi realtime sender: " + error, CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
                Log.e("DEBUG", "Realtime sender error: " + error);
            }
        });

        Query receiverQuery = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("receiver_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        realtimeUtil.listenToCustomQuery(receiverQuery, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
            @Override
            public void onDataAdded(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("DEBUG", "New message added (as receiver)");
            }

            @Override
            public void onDataModified(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("DEBUG", "Message modified (as receiver)");
            }

            @Override
            public void onDataRemoved(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("DEBUG", "Message removed (as receiver)");
            }

            @Override
            public void onError(String error) {
                CustomToast.makeText(ChatListActivity.this, "Lỗi realtime receiver: " + error, CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
                Log.e("DEBUG", "Realtime receiver error: " + error);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realtimeUtil.removeAllListeners();
    }
}
