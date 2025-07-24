package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ChatMessageAdapter;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.entities.Message;
import com.example.preely.model.request.CreateMessageRequest;
import com.example.preely.util.Constraints;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.example.preely.viewmodel.MessageService;
import com.example.preely.viewmodel.UserService;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private MessageService messageService;
    private UserService userService;
    private ChatMessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView receiverFullNameText, receiverUsernameText;  // 2 TextView riêng biệt
    private ImageView backButton;
    private SessionManager sessionManager;
    private FirestoreRealtimeUtil realtimeUtil;

    private String roomId;
    private String receiverId;
    private String receiverFullName;
    private String receiverUsername;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        getIntentData();
        initializeServices();
        initializeViews();

        if (!validateCurrentUser()) {
            return;
        }

        setupRecyclerView();
        setupMessageService();
        setupSendButton();

        // Kiểm tra session
        if (sessionManager.getUserSession() == null || sessionManager.getUserSession().getId() == null) {
            Log.e("ChatDetailActivity", "Session or user ID null");
            finish();
            return;
        }

        currentUserId = sessionManager.getUserSession().getId();

        // Fetch thông tin receiver để hiển thị (yêu cầu: hiện fullname lẫn username trên activity_chat_detail)
        if (receiverId != null && !receiverId.isEmpty()) {
            fetchReceiverInfo();
        }

        // Load tin nhắn và setup realtime
        if (roomId != null) {
            loadMessages();
            attachRealtimeListener();
        }
    }

    private void getIntentData() {
        roomId = getIntent().getStringExtra("ROOM_ID");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverFullName = getIntent().getStringExtra("RECEIVER_FULL_NAME");
        receiverUsername = getIntent().getStringExtra("RECEIVER_USERNAME");
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        realtimeUtil = new FirestoreRealtimeUtil();
        userService = new ViewModelProvider(this).get(UserService.class);
        messageService = new ViewModelProvider(this).get(MessageService.class);
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        receiverFullNameText = findViewById(R.id.receiver_full_name_detail);
        receiverUsernameText = findViewById(R.id.receiver_username_detail);
        backButton = findViewById(R.id.back_button);

        // Set thông tin từ Intent trước (nếu có)
        if (receiverFullName != null) {
            receiverFullNameText.setText(receiverFullName);
        }
        if (receiverUsername != null) {
            receiverUsernameText.setText("@" + receiverUsername);
        }

        // Setup back button
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    /**
     * Fetch thông tin receiver để hiển thị fullname và username (yêu cầu: hiện fullname lẫn username trên activity_chat_detail)
     */
    private void fetchReceiverInfo() {
        userService.getUserInfo(receiverId, new UserService.UserInfoCallback() {
            @Override
            public void onSuccess(String fullName, String username) {
                // Hiển thị fullname làm tên chính
                receiverFullNameText.setText(fullName);
                // Hiển thị username bên dưới fullname
                receiverUsernameText.setText("@" + username);

                Log.d("ChatDetailActivity", "Receiver info loaded - Full name: " + fullName + ", Username: " + username);
            }

            @Override
            public void onFailure(String error) {
                Log.e("ChatDetailActivity", "Failed to fetch receiver info: " + error);

                // Fallback to default values
                if (receiverFullNameText.getText().toString().isEmpty()) {
                    receiverFullNameText.setText("Chat");
                }
                if (receiverUsernameText.getText().toString().isEmpty()) {
                    receiverUsernameText.setText("@unknown");
                }
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        // ✅ Đảm bảo currentUserId không null trước khi tạo adapter
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e("ChatDetailActivity", "Current user ID is null, trying to get from session");

            if (sessionManager.getUserSession() != null && sessionManager.getUserSession().getId() != null) {
                currentUserId = sessionManager.getUserSession().getId();
                Log.d("ChatDetailActivity", "Retrieved current user ID from session: " + currentUserId);
            } else {
                Log.e("ChatDetailActivity", "Cannot get current user ID, redirecting to login");
                // Redirect to login or show error
                finish();
                return;
            }
        }

        // ✅ Tạo adapter với currentUserId đã validate
        adapter = new ChatMessageAdapter(messageList, currentUserId);
        recyclerView.setAdapter(adapter);

        Log.d("ChatDetailActivity", "RecyclerView setup completed with currentUserId: " + currentUserId);
    }

    private boolean validateCurrentUser() {
        if (sessionManager.getUserSession() == null || sessionManager.getUserSession().getId() == null) {
            Log.e("ChatDetailActivity", "Session or user ID null");

            // Show error dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Lỗi")
                    .setMessage("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Redirect to login
                        Intent intent = new Intent(this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();

            return false;
        }

        currentUserId = sessionManager.getUserSession().getId();
        Log.d("ChatDetailActivity", "Current user validated: " + currentUserId);
        return true;
    }

    private void setupMessageService() {
        messageService.getMessagesForRoom(roomId).observe(this, messages -> {
            if (messages != null) {
                messageList.clear();
                messageList.addAll(messages);
                adapter.notifyDataSetChanged();
                scrollToBottom();
                Log.d("ChatDetailActivity", "Messages loaded for room: " + messages.size());
            }
        });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        CreateMessageRequest request = new CreateMessageRequest();
        request.setSenderId(currentUserId);
        request.setReceiverId(receiverId);
        request.setContent(content);
        request.setRoom(roomId);
        request.setSendAt(Timestamp.now());
        request.setRead(false);

        messageService.sendMessage(request, new MessageService.SendMessageCallback() {
            @Override
            public void onSuccess() {
                messageInput.setText("");
                Log.d("ChatDetailActivity", "Message sent successfully");
            }

            @Override
            public void onFailure(String error) {
                Log.e("ChatDetailActivity", "Send message failed: " + error);
                // Có thể hiển thị Toast error ở đây
            }
        });
    }

    private void loadMessages() {
        if (roomId != null) {
            messageService.loadMessagesForRoom(roomId);
        }
    }

    private void attachRealtimeListener() {
        Query query = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("room", roomId)
                .orderBy("send_at", Query.Direction.ASCENDING);

        realtimeUtil.listenToCustomQuery(query, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
            @Override
            public void onDataAdded(Message data) {
                loadMessages();  // Reload messages khi có tin nhắn mới
            }

            @Override
            public void onDataModified(Message data) {
                loadMessages();  // Reload khi tin nhắn được sửa
            }

            @Override
            public void onDataRemoved(Message data) {
                loadMessages();  // Reload khi tin nhắn bị xóa
            }

            @Override
            public void onError(String error) {
                Log.e("ChatDetailActivity", "Realtime error in chat detail: " + error);
            }
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đánh dấu tin nhắn đã đọc khi vào room
        if (roomId != null && currentUserId != null) {
            messageService.markRoomMessagesAsRead(roomId, currentUserId, new MessageService.MarkAsReadCallback() {
                @Override
                public void onSuccess() {
                    Log.d("ChatDetailActivity", "Messages marked as read for room: " + roomId);
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ChatDetailActivity", "Failed to mark messages as read: " + error);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realtimeUtil != null) {
            realtimeUtil.removeAllListeners();
        }
    }
}
