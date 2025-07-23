package com.example.preely.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.entities.User;
import com.example.preely.model.response.UserResponse;
import com.example.preely.view.CustomToast;
import com.google.firebase.firestore.DocumentReference;
import com.bumptech.glide.Glide;
import com.example.preely.repository.MainRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar, btnEditAvatar, btnMenu, btnMore;
    private TextView tvPhone, tvName;
    private LinearLayout btnEditProfile, btnNotification,
                        btnPrivacyPolicy, btnHelpCenter,
                        btnHome, btnCart, btnOrders, btnWallet, btnProfile, btnLogout, btnChangePassword, btnSavedServices;

    private Switch switchNotification;
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize SessionManager and SharedPreferences
        sessionManager = new SessionManager(this);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Initialize views
        initViews();

        // Load trạng thái notification khi vào màn hình
        boolean notificationEnabled = sharedPreferences.getBoolean("notification_enabled", true);
        switchNotification.setChecked(notificationEnabled);
        // Lưu trạng thái khi user thay đổi
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notification_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "Đã bật thông báo" : "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
        });

        // Lấy id user từ session
        UserResponse sessionUser = sessionManager.getUserSession();
        String userId = null;
        if (sessionUser != null && sessionUser.getId() != null) {
            userId = sessionUser.getId().getId();
        }
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("user")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        Log.d("ProfileActivity", "Full user loaded from Firestore: " + user);
                        assert user != null;
                        loadUserData(user);
                    } else {
                        Log.d("ProfileActivity", "User not found in Firestore");

                    }
                });
        }

        // Set up click listeners
        setupClickListeners();

        // Load dark mode state
        loadDarkModeState();
    }

    private void initViews() {
        // Profile section
        imgAvatar = findViewById(R.id.imgAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);

        // Header buttons
        btnMenu = findViewById(R.id.btnMenu);
        btnMore = findViewById(R.id.btnMore);

        // Settings buttons
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnNotification = findViewById(R.id.btnNotification);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnHelpCenter = findViewById(R.id.btnHelpCenter);
        btnLogout = findViewById(R.id.btnLogout);
        switchNotification = findViewById(R.id.switchNotification);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSavedServices = findViewById(R.id.btnSavedServices);

        // Bottom navigation
        btnHome = findViewById(R.id.btnHome);
        btnCart = findViewById(R.id.btnCart);
        btnOrders = findViewById(R.id.btnOrders);
        btnWallet = findViewById(R.id.btnWallet);
        btnProfile = findViewById(R.id.btnProfile);
    }

    private void loadUserData(User user) {
        Log.d("ProfileActivity", "User loaded: " + user);
        Log.d("ProfileActivity", "user.getFull_name(): " + user.getFull_name());
        Log.d("ProfileActivity", "user.getPhone_number(): " + user.getPhone_number());
        Log.d("ProfileActivity", "user.getAvatar(): " + user.getAvatar());
        if (user.getFull_name() != null) {
            tvName.setText(user.getFull_name());
            Log.d("ProfileActivity", "Set tvName: " + user.getFull_name());
        } else {
            tvName.setText("N/A");
            Log.d("ProfileActivity", "Set tvName: N/A");
        }
        tvPhone.setText(user.getPhone_number());
        Log.d("ProfileActivity", "Set tvPhone: " + user.getPhone_number());
        String avatarUrl = user.getAvatar();
        Log.d("ProfileActivity", "Avatar URL: " + avatarUrl);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_image);
        }
    }

    private void setupClickListeners() {
        // Header buttons
        btnMenu.setOnClickListener(v -> onBackPressed());

        btnMore.setOnClickListener(v -> {
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show();
        });

        // Profile section
        btnEditAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "Edit avatar", Toast.LENGTH_SHORT).show();
        });

        // Settings buttons
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        });

        btnNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Notification settings", Toast.LENGTH_SHORT).show();
        });


        btnPrivacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        btnHelpCenter.setOnClickListener(v -> {
            Toast.makeText(this, "Help Center", Toast.LENGTH_SHORT).show();
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnSavedServices.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavedServicesActivity.class);
            startActivity(intent);
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            logout();
        });

        // Bottom navigation
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnCart.setOnClickListener(v -> {
            Toast.makeText(this, "Cart", Toast.LENGTH_SHORT).show();
        });

        btnOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Orders", Toast.LENGTH_SHORT).show();
        });

        btnWallet.setOnClickListener(v -> {
            Toast.makeText(this, "Wallet", Toast.LENGTH_SHORT).show();
        });

        btnProfile.setOnClickListener(v -> {
            // Already on profile screen
            Toast.makeText(this, "You are on Profile screen", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDarkModeState() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        // switchDarkMode.setChecked(isDarkMode); // This line is removed
    }

    private void toggleDarkMode(boolean isDarkMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dark_mode", isDarkMode);
        editor.apply();

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toast.makeText(this,
            isDarkMode ? "Dark mode enabled" : "Light mode enabled",
            Toast.LENGTH_SHORT).show();
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this amazing app!");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                "I'm using Preely app and I think you'll love it too! " +
                "Download it from: https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing app", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                logout();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void logout() {
        // Xóa toàn bộ session/local user
        sessionManager.clearSession();
        // Chuyển về màn hình đăng nhập, xóa hết các activity trước đó
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Navigate back to previous screen or home
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserResponse sessionUser = sessionManager.getUserSession();
        String userId = null;
        if (sessionUser != null && sessionUser.getId() != null) {
            userId = sessionUser.getId().getId();
        }
        if (userId != null) {
            FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            Log.d("ProfileActivity", "Full user loaded from Firestore: " + user);
                            assert user != null;
                            loadUserData(user);
                        } else {
                            Log.d("ProfileActivity", "User not found in Firestore");

                        }
                    });
        }
    }
} 