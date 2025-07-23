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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private MessageService messageService;
    private ChatMessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView receiverNameText;
    private ImageView backButton, paymentButton;
    private SessionManager sessionManager;
    private FirestoreRealtimeUtil realtimeUtil;
    private UserService userService;
    private String roomId;
    private String receiverId;
    private String receiverName;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        roomId = getIntent().getStringExtra("ROOM_ID");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");

        sessionManager = new SessionManager(this);
        realtimeUtil = new FirestoreRealtimeUtil(this);
        userService = new UserService();

        if (sessionManager.getUserSession() == null || sessionManager.getUserSession().getId() == null) {
            Log.e("ChatDetailActivity", "Session or user ID null");
            finish();
            return;
        }

        currentUserId = sessionManager.getUserSession().getId().getId();

        initViews();
        setupRecyclerView();
        setupMessageService();
        setupSendButton();
        setupPaymentButton();

        if (receiverId != null && !receiverId.isEmpty()) {
            fetchReceiverName();
        }

        if (roomId != null) {
            loadMessages();
            attachRealtimeListener();
        }
    }

    private void fetchReceiverName() {
        userService.getUserName(receiverId, new UserService.UserNameCallback() {
            @Override
            public void onSuccess(String userName) {
                if (receiverNameText != null) {
                    receiverNameText.setText(userName);
                }
                Log.d("DEBUG", "Receiver name updated: " + userName);
            }

            @Override
            public void onFailure(String error) {
                Log.e("DEBUG", "Failed to fetch receiver name: " + error);
                if (receiverNameText != null && (receiverName == null || receiverName.equals("Unknown"))) {
                    receiverNameText.setText("Chat");
                }
            }
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        receiverNameText = findViewById(R.id.receiver_name_text);
        backButton = findViewById(R.id.back_button);
        paymentButton = findViewById(R.id.payment_button);

        if (receiverNameText != null) {
            receiverNameText.setText(receiverName != null ? receiverName : "Unknown");
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Hiển thị tin nhắn mới nhất ở cuối
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatMessageAdapter(messageList, currentUserId);
        recyclerView.setAdapter(adapter);
    }

    private void setupMessageService() {
        messageService = new ViewModelProvider(this).get(MessageService.class);

        messageService.getMessagesForRoom(roomId).observe(this, messages -> {
            if (messages != null) {
                messageList.clear();
                messageList.addAll(messages);
                adapter.notifyDataSetChanged();
                scrollToBottom();
                Log.d("DEBUG", "Messages loaded for room: " + messages.size());
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
                Log.d("DEBUG", "Message sent successfully");
            }

            @Override
            public void onFailure(String error) {
                CustomToast.makeText(ChatDetailActivity.this, "Lỗi gửi tin nhắn: " + error,
                        CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
                Log.e("DEBUG", "Send message failed: " + error);
            }
        });
    }

    private void loadMessages() {
        if (roomId != null) {
            messageService.loadMessagesForRoom(roomId);
        }
    }

    private void attachRealtimeListener() {
        Query query = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("room", roomId)
                .orderBy("send_at", Query.Direction.ASCENDING);

        realtimeUtil.listenToCustomQuery(query, Message.class, new FirestoreRealtimeUtil.RealtimeListener<Message>() {
            @Override
            public void onDataAdded(Message data) {
                loadMessages();
            }

            @Override
            public void onDataModified(Message data) {
                loadMessages();
            }

            @Override
            public void onDataRemoved(Message data) {
                loadMessages();
            }

            @Override
            public void onError(String error) {
                Log.e("DEBUG", "Realtime error in chat detail: " + error);
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
        if (roomId != null && currentUserId != null) {
            messageService.markRoomMessagesAsRead(roomId, currentUserId, new MessageService.MarkAsReadCallback() {
                @Override
                public void onSuccess() {
                    Log.d("DEBUG", "Messages marked as read for room: " + roomId);
                }

                @Override
                public void onFailure(String error) {
                    Log.e("DEBUG", "Failed to mark messages as read: " + error);
                }
            });
        }
    }

    private void setupPaymentButton() {
        // Payment button in toolbar
        if (paymentButton != null) {
            paymentButton.setOnClickListener(v -> openTransactionActivity());
        }
    }

    private void openTransactionActivity() {
        if (receiverId == null || receiverId.isEmpty()) {
            CustomToast.makeText(this, "Không thể xác định người nhận thanh toán",
                    CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
            return;
        }

        try {
            Intent intent = new Intent(ChatDetailActivity.this, TransactionActivity.class);

            intent.putExtra("RECEIVER_ID", receiverId);
            intent.putExtra("RECEIVER_NAME", receiverName);
            intent.putExtra("FROM_CHAT", true);

            intent.putExtra("ROOM_ID", roomId);

            Log.d("ChatDetailActivity", "Opening transaction with receiver: " + receiverName + " (ID: " + receiverId + ")");

            startActivityForResult(intent, 100); // Request code 100 cho transaction

        } catch (Exception e) {
            Log.e("ChatDetailActivity", "Error opening transaction activity", e);
            CustomToast.makeText(this, "Lỗi mở trang thanh toán: " + e.getMessage(),
                    CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) { // Transaction request code
            if (resultCode == RESULT_OK && data != null) {
                // Nhận kết quả từ TransactionActivity
                String transactionStatus = data.getStringExtra("transaction_status");
                String transactionAmount = data.getStringExtra("transaction_amount");

                if ("Paid".equals(transactionStatus)) {
                    // Transaction thành công - có thể gửi tin nhắn thông báo
                    sendTransactionSuccessMessage(transactionAmount);

                    CustomToast.makeText(this, "Thanh toán thành công!",
                            CustomToast.LENGTH_SHORT, Constraints.NotificationType.SUCCESS).show();
                } else if ("Failed".equals(transactionStatus)) {
                    CustomToast.makeText(this, "Thanh toán thất bại",
                            CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
                }
            }
        }
    }

    private void sendTransactionSuccessMessage(String amount) {
        if (TextUtils.isEmpty(amount)) return;

        String transactionMessage = "💰 Đã thanh toán " + amount + " VND thành công!";

        CreateMessageRequest request = new CreateMessageRequest();
        request.setSenderId(currentUserId);
        request.setReceiverId(receiverId);
        request.setContent(transactionMessage);
        request.setRoom(roomId);
        request.setSendAt(Timestamp.now());
        request.setRead(false);

        messageService.sendMessage(request, new MessageService.SendMessageCallback() {
            @Override
            public void onSuccess() {
                Log.d("ChatDetailActivity", "Transaction success message sent");
            }

            @Override
            public void onFailure(String error) {
                Log.e("ChatDetailActivity", "Failed to send transaction message: " + error);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realtimeUtil != null) {
            realtimeUtil.removeAllListeners();
        }
    }
}
