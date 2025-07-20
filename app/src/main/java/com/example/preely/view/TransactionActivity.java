package com.example.preely.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.preely.R;
import com.example.preely.model.entities.User;
import com.example.preely.model.entities.Post;
import com.example.preely.model.entities.Transaction;
import com.example.preely.authentication.SessionManager;
import com.example.preely.viewmodel.TransactionService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionActivity extends AppCompatActivity {
    private AutoCompleteTextView giverDropdown, postDropdown;
    private EditText amountInput;
    private TextView statusTv, transactionDateTv;
    private MaterialButton btnPayVNPay;
    private ProgressDialog progressDialog;
    private List<User> userList = new ArrayList<>();
    private List<Post> postList = new ArrayList<>();
    private List<User> filteredUserList = new ArrayList<>();
    private List<Post> filteredPostList = new ArrayList<>();
    private ArrayAdapter<String> userAdapter;
    private ArrayAdapter<String> postAdapter;
    private String selectedGiverId = null;
    private String selectedPostId = null;
    private String requesterId;
    private TransactionService transactionService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // Initialize views
        giverDropdown = findViewById(R.id.giver_dropdown);
        postDropdown = findViewById(R.id.post_dropdown);
        amountInput = findViewById(R.id.amount_input);
        statusTv = findViewById(R.id.status_tv);
        transactionDateTv = findViewById(R.id.transaction_date_tv);
        btnPayVNPay = findViewById(R.id.btn_pay_vnpay);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCancelable(false);

        // Get requester ID from session
        SessionManager sessionManager = new SessionManager(this);
        requesterId = sessionManager.getUserSession().getId().getId();
        if (requesterId == null) {
            Toast.makeText(this, getString(R.string.please_login_again), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set current date
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        transactionDateTv.setText(getString(R.string.transaction_date, today));

        // Initially disable post dropdown until giver is selected
        postDropdown.setEnabled(false);

        // Load users và posts
        transactionService = new ViewModelProvider(this).get(TransactionService.class);
        loadUsers();
        loadPosts();

        setupDropdownListeners();
        btnPayVNPay.setOnClickListener(v -> handlePayVNPay());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.hasExtra("transaction")) {
            Transaction transaction = (Transaction) data.getSerializableExtra("transaction");
            if (transaction != null) {
                statusTv.setText("Status: " + transaction.getStatus());
                Toast.makeText(this, "Payment result: " + transaction.getStatus(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadUsers() {
        progressDialog.show();
        transactionService.getAllUsers().observe(this, users -> {
            progressDialog.dismiss();
            if (users != null) {
                userList.clear();
                for (User u : users) {
                    if (u != null && u.getId() != null && !u.getId().equals(requesterId) && u.getFull_name() != null) {
                        userList.add(u);
                    }
                }
                List<String> names = new ArrayList<>();
                for (User u : userList) {
                    if (u.getFull_name() != null) {
                        names.add(u.getFull_name());
                    }
                }
                userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                giverDropdown.setAdapter(userAdapter);
                giverDropdown.setThreshold(1);
                Log.d("TransactionActivity", "Loaded users: " + names.size());
                debugAllUsers(); // Debug all users
                if (names.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_users_to_select), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.cannot_load_users), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void debugAllUsers() {
        Log.d("TransactionActivity", "=== DEBUG ALL USERS ===");
        for (int i = 0; i < userList.size(); i++) {
            User u = userList.get(i);
            if (u != null) {
                Log.d("TransactionActivity", "User " + i + ": ID=" + u.getId() + 
                      ", Name=" + u.getFull_name() + 
                      ", Email=" + u.getEmail());
            }
        }
        Log.d("TransactionActivity", "=== END DEBUG USERS ===");
    }

    private void debugAllPosts() {
        Log.d("TransactionActivity", "=== DEBUG ALL POSTS ===");
        for (int i = 0; i < postList.size(); i++) {
            Post p = postList.get(i);
            if (p != null) {
                Log.d("TransactionActivity", "Post " + i + ": ID=" + p.getId() +
                      ", Title=" + p.getTitle() + 
                      ", Seller_id=" + p.getSeller_id());
            }
        }
        Log.d("TransactionActivity", "=== END DEBUG ===");
    }

    private void loadPosts() {
        progressDialog.show();
        Log.d("TransactionActivity", "Starting to load posts...");
        transactionService.getAllPosts().observe(this, posts -> {
            progressDialog.dismiss();
            Log.d("TransactionActivity", "Posts response received. Raw posts count: " + (posts != null ? posts.size() : 0));
            
            if (posts != null) {
                postList.clear();
                int validPosts = 0;
                int invalidPosts = 0;
                
                for (Post p : posts) {
                    if (p != null && p.getId() != null && p.getTitle() != null) {
                        postList.add(p);
                        validPosts++;
                        Log.d("TransactionActivity", "Valid post added: " + p.getTitle() + " with seller_id: " + p.getSeller_id());
                    } else {
                        invalidPosts++;
                        if (p != null) {
                            Log.e("TransactionActivity", "Invalid post - ID: " + p.getId() + ", Title: " + p.getTitle() + ", Seller_id: " + p.getSeller_id());
                        } else {
                            Log.e("TransactionActivity", "Post is null");
                        }
                    }
                }
                
                Log.d("TransactionActivity", "Posts processing complete - Valid: " + validPosts + ", Invalid: " + invalidPosts);
                
                // Don't show all posts initially, wait for giver selection
                postAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
                postDropdown.setAdapter(postAdapter);
                Log.d("TransactionActivity", "Loaded posts: " + postList.size());
                debugAllPosts(); // Debug all posts
                if (postList.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_posts_to_select), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("TransactionActivity", "Posts response is null");
                Toast.makeText(this, getString(R.string.cannot_load_posts), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDropdownListeners() {
        giverDropdown.setOnClickListener(v -> {
            if (userAdapter != null && userAdapter.getCount() > 0) {
                giverDropdown.showDropDown();
                Log.d("TransactionActivity", "Show user dropdown: " + userAdapter.getCount());
            } else {
                Toast.makeText(this, getString(R.string.no_users_to_select), Toast.LENGTH_SHORT).show();
            }
        });
        giverDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && userAdapter != null && userAdapter.getCount() > 0) {
                giverDropdown.showDropDown();
            }
        });
        giverDropdown.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (position >= 0 && position < parent.getCount()) {
                    String name = (String) parent.getItemAtPosition(position);
                    // Use filtered list if there's a filter, otherwise use original list
                    List<User> searchList = filteredUserList.isEmpty() ? userList : filteredUserList;
                    for (User u : searchList) {
                        if (u.getFull_name() != null && u.getFull_name().equals(name)) {
                            selectedGiverId = u.getId().getId();
                            Log.d("TransactionActivity", "Selected user: " + name + " with ID: " + selectedGiverId);
                            Log.d("TransactionActivity", "User details - Full name: " + u.getFull_name() + ", ID: " + u.getId() + ", Email: " + u.getEmail());
                            
                            // Clear previous post selection
                            selectedPostId = null;
                            postDropdown.setText("");
                            
                            // Enable post dropdown first
                            postDropdown.setEnabled(true);
                            postDropdown.setHint(getString(R.string.select_post));
                            
                            // Then filter and load posts
                            loadPostsForGiver(selectedGiverId);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TransactionActivity", "Error selecting user", e);
                Toast.makeText(this, getString(R.string.error_selecting_user), Toast.LENGTH_SHORT).show();
            }
        });
        giverDropdown.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUserDropdown(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        postDropdown.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Post dropdown clicked. Enabled: " + postDropdown.isEnabled() + 
                  ", Adapter count: " + (postAdapter != null ? postAdapter.getCount() : 0) + 
                  ", Selected giver: " + selectedGiverId);
            
            if (selectedGiverId == null) {
                Toast.makeText(this, getString(R.string.select_giver_first), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (postAdapter != null && postAdapter.getCount() > 0) {
                postDropdown.showDropDown();
                Log.d("TransactionActivity", "Show post dropdown: " + postAdapter.getCount());
            } else {
                Toast.makeText(this, getString(R.string.no_posts_to_select), Toast.LENGTH_SHORT).show();
            }
        });
        postDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("TransactionActivity", "Post dropdown focus changed: " + hasFocus + 
                  ", Enabled: " + postDropdown.isEnabled() + 
                  ", Adapter count: " + (postAdapter != null ? postAdapter.getCount() : 0));
            
            if (hasFocus && selectedGiverId != null && postAdapter != null && postAdapter.getCount() > 0) {
                postDropdown.showDropDown();
            }
        });
        postDropdown.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (position >= 0 && position < parent.getCount()) {
                    String title = (String) parent.getItemAtPosition(position);
                    Log.d("TransactionActivity", "Selected post title: " + title);
                    
                    // Find the post in the filtered list
                    for (Post p : postList) {
                        if (p != null && p.getSeller_id() != null) {
                            if (p.getSeller_id().equals(selectedGiverId) 
                                && p.getTitle() != null && p.getTitle().equals(title)) {
                                selectedPostId = p.getId().getId();
                                Log.d("TransactionActivity", "Selected post: " + title + " with ID: " + selectedPostId);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TransactionActivity", "Error selecting post", e);
                Toast.makeText(this, getString(R.string.error_selecting_post), Toast.LENGTH_SHORT).show();
            }
        });
        postDropdown.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPostDropdown(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterUserDropdown(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            // If no query, use original list
            userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, 
                userList.stream().map(User::getFull_name).filter(Objects::nonNull).collect(Collectors.toList()));
        } else {
            // Filter based on query
            for (User u : userList) {
                if (u.getFull_name() != null && u.getFull_name().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(u);
                }
            }
            List<String> names = new ArrayList<>();
            for (User u : filteredUserList) {
                if (u.getFull_name() != null) {
                    names.add(u.getFull_name());
                }
            }
            userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        }
        giverDropdown.setAdapter(userAdapter);
        if (!query.isEmpty()) {
            giverDropdown.showDropDown();
        }
    }
    
    private void filterPostDropdown(String query) {
        if (selectedGiverId == null) {
            Toast.makeText(this, getString(R.string.select_giver_first), Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d("TransactionActivity", "Filtering posts with query: " + query + " for giver: " + selectedGiverId);
        
        List<Post> filteredPosts = new ArrayList<>();
        for (Post p : postList) {
            if (p != null && p.getSeller_id() != null) {
                if (p.getSeller_id().equals(selectedGiverId)) {
                    if (query.isEmpty() || (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase()))) {
                        filteredPosts.add(p);
                    }
                }
            }
        }
        
        List<String> titles = new ArrayList<>();
        for (Post p : filteredPosts) {
            if (p.getTitle() != null) {
                titles.add(p.getTitle());
            }
        }
        
        postAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        postDropdown.setAdapter(postAdapter);
        
        if (!query.isEmpty() && !titles.isEmpty()) {
            postDropdown.showDropDown();
        }
        
        Log.d("TransactionActivity", "Filtered posts count: " + titles.size());
    }

    @SuppressLint("StringFormatInvalid")
    private void loadPostsForGiver(String giverId) {
        Log.d("TransactionActivity", "Loading posts for giver ID: " + giverId);
        Log.d("TransactionActivity", "Total posts available: " + postList.size());
        
        // Filter posts by seller_id (which is a DocumentReference path like "/user/aK9Fun5gK5DDVYYpLpiU")
        List<Post> giverPosts = new ArrayList<>();
        int checkedPosts = 0;
        int matchedPosts = 0;
        
        for (Post p : postList) {
            checkedPosts++;
            if (p != null && p.getSeller_id() != null) {
                Log.d("TransactionActivity", "Checking post " + checkedPosts + ": " + p.getTitle() + 
                      " | Seller ID: " + p.getSeller_id() + 
                      " | Target giver: " + giverId + 
                      " | Match: " + p.getSeller_id().equals(giverId));
                
                if (p.getSeller_id().equals(giverId)) {
                    giverPosts.add(p);
                    matchedPosts++;
                    Log.d("TransactionActivity", "✓ MATCHED post: " + p.getTitle() + " with seller ID: " + p.getSeller_id());
                }
            } else {
                Log.e("TransactionActivity", "Post " + checkedPosts + " is null or has null seller_id");
            }
        }
        
        Log.d("TransactionActivity", "Posts matching summary - Checked: " + checkedPosts + ", Matched: " + matchedPosts);
        
        // Create titles list
        List<String> titles = new ArrayList<>();
        for (Post p : giverPosts) {
            if (p.getTitle() != null) {
                titles.add(p.getTitle());
            }
        }
        
        Log.d("TransactionActivity", "Posts found for giver: " + titles.size());
        
        // Update adapter and dropdown
        postAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        postDropdown.setAdapter(postAdapter);
        postDropdown.setThreshold(1); // Show dropdown after 1 character
        
        if (titles.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_posts_found_for_giver, giverId), Toast.LENGTH_SHORT).show();
            Log.d("TransactionActivity", "No posts found for giver: " + giverId);
        } else {
            Log.d("TransactionActivity", "Successfully loaded " + titles.size() + " posts for giver: " + giverId);
            // Show dropdown automatically
            postDropdown.showDropDown();
        }
    }



    @SuppressLint("StringFormatInvalid")
    private void filterPostsByGiver(String giverId) {
        Log.d("TransactionActivity", "Filtering posts for giver ID: " + giverId);
        Log.d("TransactionActivity", "Total posts available: " + postList.size());
        
        filteredPostList.clear();
        for (Post p : postList) {
            // Check seller_id field
            boolean isGiverPost = false;
            if (p != null && p.getSeller_id() != null) {
                if (p.getSeller_id().equals(giverId)) {
                    isGiverPost = true;
                    Log.d("TransactionActivity", "Found post by seller_id: " + p.getTitle());
                }
            }
            
            if (isGiverPost) {
                filteredPostList.add(p);
                Log.d("TransactionActivity", "Found post: " + p.getTitle() + " with seller ID: " + p.getSeller_id());
            }
        }
        
        List<String> titles = new ArrayList<>();
        for (Post p : filteredPostList) {
            if (p.getTitle() != null) {
                titles.add(p.getTitle());
            }
        }
        
        Log.d("TransactionActivity", "Filtered posts count: " + titles.size());
        
        postAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        postDropdown.setAdapter(postAdapter);
        
        if (titles.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_posts_found_for_giver, giverId), Toast.LENGTH_SHORT).show();
            Log.d("TransactionActivity", "No posts found for giver: " + giverId);
        } else {
            postDropdown.showDropDown();
            Log.d("TransactionActivity", "Filtered posts for giver " + giverId + ": " + titles.size());
        }
    }

    private void handlePayVNPay() {
        String amountStr = amountInput.getText().toString().trim();
        if (selectedGiverId == null) {
            Toast.makeText(this, getString(R.string.please_select_giver), Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedPostId == null) {
            Toast.makeText(this, getString(R.string.please_select_post), Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        final double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo transaction object
        Transaction transaction = new Transaction();
        transaction.setRequester_id(requesterId);
        transaction.setGiver_id(selectedGiverId);
        transaction.setPost_id(selectedPostId);
        transaction.setAmount(amount);
        transaction.setStatus("Unpaid");
        transaction.setTransaction_date(Timestamp.now());

        // Lưu transaction vào Firestore trước khi thanh toán
        transactionService.saveTransaction(transaction, new TransactionService.TransactionCallback() {
            @Override
            public void onSuccess(Transaction savedTransaction) {
                startVNPayActivity(savedTransaction, amount);
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(TransactionActivity.this, getString(R.string.create_transaction_error, error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startVNPayActivity(Transaction savedTransaction, double amount) {
        Intent intent = new Intent(TransactionActivity.this, VNPayActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("orderInfo", getString(R.string.order_info, getPostTitleById(selectedPostId)));
        intent.putExtra("transaction", savedTransaction);
        intent.putExtra("requesterId", requesterId);
        intent.putExtra("giverId", selectedGiverId);
        intent.putExtra("postId", selectedPostId);
        startActivityForResult(intent, 1);
    }

    private String getPostTitleById(String postId) {
        for (Post p : postList) {
            if (p.getId().equals(postId)) return p.getTitle();
        }
        return "";
    }
} 