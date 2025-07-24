package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.preely.model.response.UserResponse;
import com.example.preely.viewmodel.UserService;

import java.util.ArrayList;
import java.util.List;

public class UserSearchActivity extends AppCompatActivity {
    private static final String TAG = "UserSearchActivity";

    private EditText searchInput;
    private RecyclerView userRecyclerView;
    private TextView emptyText;
    private ProgressBar progressBar;
    private ImageView backButton;

    private UserService userService;
    private SessionManager sessionManager;
    private UserSearchAdapter adapter;
    private List<UserResponse> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        initializeServices();
        initializeViews();
        setupRecyclerView();
        setupSearchInput();
        setupBackButton();
    }

    private void initializeServices() {
        userService = new ViewModelProvider(this).get(UserService.class);
        sessionManager = new SessionManager(this);
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.search_input);
        userRecyclerView = findViewById(R.id.user_recycler_view);
        emptyText = findViewById(R.id.empty_text);
        progressBar = findViewById(R.id.progress_bar);
        backButton = findViewById(R.id.back_button);
    }

    private void setupRecyclerView() {
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSearchAdapter(userList, this::startChatWithUser);
        userRecyclerView.setAdapter(adapter);
    }

    private void setupSearchInput() {
        // Handle search action từ keyboard
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

        // Text change listener cho real-time search
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 2) {  // Tìm kiếm từ 2 ký tự trở lên
                    searchUsers(query);
                } else if (query.isEmpty()) {
                    // Clear results khi search được xóa
                    userList.clear();
                    adapter.notifyDataSetChanged();
                    showEmptyState("Nhập tên người dùng để tìm kiếm");
                }
            }
        });
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Tìm kiếm user theo cả fullname và username
     */
    private void searchUsers(String query) {
        showLoading();

        userService.searchUsers(query, new UserService.UserSearchCallback() {
            @Override
            public void onSuccess(List<UserResponse> users) {
                if (users.isEmpty()) {
                    showEmptyState("Không tìm thấy người dùng nào");
                } else {
                    // Filter bỏ current user
                    if (sessionManager.getUserSession() != null) {
                        String currentUserId = sessionManager.getUserSession().getId();
                        List<UserResponse> filteredUsers = new ArrayList<>();

                        for (UserResponse user : users) {
                            if (!user.getId().equals(currentUserId)) {
                                filteredUsers.add(user);
                            }
                        }

                        if (filteredUsers.isEmpty()) {
                            showEmptyState("Không tìm thấy người dùng nào");
                        } else {
                            showResults(filteredUsers);
                        }
                    } else {
                        showResults(users);
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

        Log.d(TAG, "Showing " + users.size() + " search results");
    }

    /**
     * Bắt đầu chat với user được chọn
     */
    private void startChatWithUser(UserResponse selectedUser) {
        if (sessionManager.getUserSession() == null) {
            Log.e(TAG, "Session null when starting chat");
            return;
        }

        String currentUserId = sessionManager.getUserSession().getId();
        String receiverId = selectedUser.getId();

        String roomId = generateRoomId(currentUserId, receiverId);

        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("ROOM_ID", roomId);
        intent.putExtra("RECEIVER_ID", receiverId);
        intent.putExtra("RECEIVER_FULL_NAME", selectedUser.getFull_name());
        intent.putExtra("RECEIVER_USERNAME", selectedUser.getUsername());

        startActivity(intent);
        finish();
    }

    /**
     * Generate consistent room ID cho 2 user
     */
    private String generateRoomId(String userId1, String userId2) {
        // Đảm bảo room ID consistent bất kể ai initiate
        String[] ids = {userId1, userId2};
        java.util.Arrays.sort(ids);
        return ids[0] + "_" + ids[1];
    }
}
