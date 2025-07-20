package com.example.preely.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.view.adapter.MessageAdapter;
import com.example.preely.viewmodel.ChatViewModel;

public class ChatActivity extends AppCompatActivity {
    private ChatViewModel viewModel;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;
    private TextView userNameTv, userEmailTv;
    private ImageView userAvatarIv;
    private String receiverId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sessionManager = new SessionManager(this);
        receiverId = getIntent().getStringExtra("RECEIVER_ID");

        // Bind views
        recyclerView = findViewById(R.id.recycler_view_messages);
        messageInput = findViewById(R.id.edit_text_message);
        sendButton = findViewById(R.id.button_send);
        userNameTv = findViewById(R.id.text_view_user_name);
        userEmailTv = findViewById(R.id.text_view_user_email);
        userAvatarIv = findViewById(R.id.image_view_avatar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MessageAdapter adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.initChat(receiverId, sessionManager.getUserSession().getId().getId());

        // Observe data
        viewModel.getMessages().observe(this, messages -> {
            adapter.submitList(messages);
            recyclerView.scrollToPosition(messages.size() - 1);
        });
        viewModel.getReceiverUser().observe(this, user -> {
            userNameTv.setText(user.getFull_name());
            userEmailTv.setText(user.getEmail());
            userAvatarIv.setImageResource(R.drawable.ic_person);  // Avatar default
        });

        // Send button
        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (!content.isEmpty()) {
                viewModel.sendMessage(content, sessionManager.getUserSession().getId().getId(), receiverId);
                messageInput.setText("");
            }
        });
    }
}
