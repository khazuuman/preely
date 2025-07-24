package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatListActivity extends AppCompatActivity {

    private MessageService messageService;
    private ChatRoomAdapter adapter;
    private List<ChatRoomResponse> chatRoomList = new ArrayList<>();
    private List<ChatRoomResponse> originalChatRoomList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private SessionManager sessionManager;
    private FirestoreRealtimeUtil realtimeUtil;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        initializeViews();
        setupRecyclerView();
        setupServices();
        setupSearchFunctionality();
        setupFAB();

        // Kiểm tra session và load dữ liệu
        if (sessionManager.getUserSession() == null || sessionManager.getUserSession().getId() == null) {
            Log.e("ChatListActivity", "Session or user ID null");
            showEmptyState();
            return;
        }

        loadChatRooms();
        setupRealtimeListeners();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.chat_room_recycler_view);
        emptyTextView = findViewById(R.id.empty_chat_text);
        searchView = findViewById(R.id.search_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatRoomAdapter(chatRoomList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupServices() {
        sessionManager = new SessionManager(this);
        realtimeUtil = new FirestoreRealtimeUtil();
        messageService = new ViewModelProvider(this).get(MessageService.class);

        observeChatRooms();
    }

    private void setupFAB() {
        FloatingActionButton fabNewMessage = findViewById(R.id.fab_new_message);
        fabNewMessage.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, UserSearchActivity.class);
            startActivity(intent);
        });
    }

    private void observeChatRooms() {
        messageService.getChatRoomsLiveData().observe(this, rooms -> {
            if (rooms != null && !rooms.isEmpty()) {
                Log.d("ChatListActivity", "=== Chat Rooms Loaded ===");

                // Backup original list cho search
                originalChatRoomList.clear();
                originalChatRoomList.addAll(rooms);

                // Update current list
                chatRoomList.clear();
                chatRoomList.addAll(rooms);
                adapter.notifyDataSetChanged();

                // Show/hide empty state
                showChatRooms();

                Log.d("ChatListActivity", "Chat rooms loaded: " + rooms.size());

                // Debug info
                for (ChatRoomResponse room : rooms) {
                    Log.d("ChatListActivity", "Room: " + room.getRoomId() +
                            ", Full Name: " + room.getReceiverFullName() +
                            ", Username: " + room.getReceiverUsername());
                }
            } else {
                showEmptyState();
                Log.d("ChatListActivity", "No chat rooms found");
            }
        });
    }

    private void loadChatRooms() {
        String userId = sessionManager.getUserSession().getId();
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .document(userId);

        Log.d("ChatListActivity", "Loading chat rooms for user: " + userId);
        messageService.getChatRooms(currentUserRef);
    }

    private void setupRealtimeListeners() {
        String userId = sessionManager.getUserSession().getId();
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .document(userId);

        // Listener cho tin nhắn mà user là sender
        Query senderQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("sender_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        realtimeUtil.listenToCustomQuery(senderQuery, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
            @Override
            public void onDataAdded(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("ChatListActivity", "New message added (as sender)");
            }

            @Override
            public void onDataModified(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("ChatListActivity", "Message modified (as sender)");
            }

            @Override
            public void onDataRemoved(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("ChatListActivity", "Message removed (as sender)");
            }

            @Override
            public void onError(String error) {
                Log.e("ChatListActivity", "Realtime sender error: " + error);
            }
        });

        // Listener cho tin nhắn mà user là receiver
        Query receiverQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("receiver_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        realtimeUtil.listenToCustomQuery(receiverQuery, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
            @Override
            public void onDataAdded(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("ChatListActivity", "New message added (as receiver)");
            }

            @Override
            public void onDataModified(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("ChatListActivity", "Message modified (as receiver)");
            }

            @Override
            public void onDataRemoved(Message data) {
                messageService.getChatRooms(currentUserRef);
                Log.d("ChatListActivity", "Message removed (as receiver)");
            }

            @Override
            public void onError(String error) {
                Log.e("ChatListActivity", "Realtime receiver error: " + error);
            }
        });
    }

    /**
     * Setup tìm kiếm - có thể filter theo cả fullname và username (yêu cầu cuối)
     */
    private void setupSearchFunctionality() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterChatRooms(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterChatRooms(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            filterChatRooms("");  // Reset về danh sách gốc
            return false;
        });
    }

    /**
     * Filter chat rooms theo cả full name và username
     */
    private void filterChatRooms(String query) {
        // Backup original list nếu chưa có
        if (originalChatRoomList.isEmpty() && !chatRoomList.isEmpty()) {
            originalChatRoomList.addAll(chatRoomList);
        }

        if (query.isEmpty()) {
            // Reset về danh sách gốc
            chatRoomList.clear();
            chatRoomList.addAll(originalChatRoomList);
        } else {
            // Filter theo cả fullName và username (yêu cầu cuối cùng)
            List<ChatRoomResponse> filteredList = originalChatRoomList.stream()
                    .filter(room -> {
                        String queryLower = query.toLowerCase();

                        // Tìm kiếm trong full name
                        boolean matchFullName = room.getReceiverFullName() != null &&
                                room.getReceiverFullName().toLowerCase().contains(queryLower);

                        // Tìm kiếm trong username
                        boolean matchUsername = room.getReceiverUsername() != null &&
                                room.getReceiverUsername().toLowerCase().contains(queryLower);

                        return matchFullName || matchUsername;
                    })
                    .collect(Collectors.toList());

            chatRoomList.clear();
            chatRoomList.addAll(filteredList);
        }

        adapter.notifyDataSetChanged();

        // Update UI state
        if (chatRoomList.isEmpty()) {
            emptyTextView.setText("Không tìm thấy cuộc trò chuyện nào");
            showEmptyState();
        } else {
            showChatRooms();
        }

        Log.d("ChatListActivity", "Filtered " + chatRoomList.size() + " rooms for query: " + query);
    }

    private void showEmptyState() {
        emptyTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showChatRooms() {
        emptyTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realtimeUtil != null) {
            realtimeUtil.removeAllListeners();
        }
    }
}
