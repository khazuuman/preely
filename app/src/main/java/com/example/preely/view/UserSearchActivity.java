package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.UserSearchAdapter;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.entities.User;
import com.example.preely.model.response.UserResponse;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.MessageService;
import com.example.preely.viewmodel.UserService;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSearchActivity extends AppCompatActivity {
    private static final String TAG = "UserSearchActivity";

    private EditText searchInput;
    private RecyclerView userRecyclerView;
    private TextView emptyText;
    private ProgressBar progressBar;
    private ImageView backButton;

    private UserService userService;
    private MessageService messageService;
    private SessionManager sessionManager;

    private UserSearchAdapter adapter;
    private List<UserResponse> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        // Initialize services
        userService = new ViewModelProvider(this).get(UserService.class);
        messageService = new ViewModelProvider(this).get(MessageService.class);
        sessionManager = new SessionManager(this);

        // Initialize views
        searchInput = findViewById(R.id.search_input);
        userRecyclerView = findViewById(R.id.user_recycler_view);
        emptyText = findViewById(R.id.empty_text);
        progressBar = findViewById(R.id.progress_bar);
        backButton = findViewById(R.id.back_button);

        // Setup RecyclerView
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSearchAdapter(userList, this::startChatWithUser);
        userRecyclerView.setAdapter(adapter);

        // Setup search input
        setupSearchInput();

        // Setup back button
        backButton.setOnClickListener(v -> finish());
    }

    private void setupSearchInput() {
        // Handle search action from keyboard
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchInput.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchUsers(query);
                }
                return true;
            }
            return false;
        });

        // Text change listener for real-time search
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 3) {
                    searchUsers(query);
                } else if (query.isEmpty()) {
                    // Clear results when search is cleared
                    userList.clear();
                    adapter.notifyDataSetChanged();
                    showEmptyState("Nhập tên người dùng để tìm kiếm");
                }
            }
        });
    }

    private void searchUsers(String query) {
        showLoading();

        userService.searchUsers(query, new UserService.UserSearchCallback() {
            @Override
            public void onSuccess(List<UserResponse> users) {
                if (users.isEmpty()) {
                    showEmptyState("Không tìm thấy người dùng nào");
                } else {
                    // Filter out current user
                    String currentUserId = sessionManager.getUserSession().getId().getId();
                    List<UserResponse> filteredUsers = new ArrayList<>();

                    for (UserResponse user : users) {
                        if (!user.getId().getId().equals(currentUserId)) {
                            filteredUsers.add(user);
                        }
                    }

                    if (filteredUsers.isEmpty()) {
                        showEmptyState("Không tìm thấy người dùng nào");
                    } else {
                        showResults(filteredUsers);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                showEmptyState("Lỗi tìm kiếm: " + error);
                Log.e(TAG, "Search error: " + error);
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        userRecyclerView.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        progressBar.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(message);
        userRecyclerView.setVisibility(View.GONE);
    }

    private void showResults(List<UserResponse> users) {
        progressBar.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        userRecyclerView.setVisibility(View.VISIBLE);

        userList.clear();
        userList.addAll(users);
        adapter.notifyDataSetChanged();
    }

    private void startChatWithUser(UserResponse selectedUser) {
        String currentUserId = sessionManager.getUserSession().getId().getId();
        String receiverId = selectedUser.getId().getId();

        String roomId = generateRoomId(currentUserId, receiverId);

        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("ROOM_ID", roomId);
        intent.putExtra("RECEIVER_ID", receiverId);
        intent.putExtra("RECEIVER_NAME", selectedUser.getFull_name() != null ?
                selectedUser.getFull_name() : selectedUser.getUsername());
        startActivity(intent);
        finish();
    }

    private String generateRoomId(String userId1, String userId2) {
        // Ensure consistent room ID regardless of who initiates
        String[] ids = {userId1, userId2};
        java.util.Arrays.sort(ids);
        return ids[0] + "_" + ids[1];
    }
}
