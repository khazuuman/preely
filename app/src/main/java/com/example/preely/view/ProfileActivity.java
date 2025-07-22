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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.view.CustomToast;

public class ProfileActivity extends AppCompatActivity {
    
    private ImageView imgAvatar, btnEditAvatar, btnMenu, btnMore;
    private TextView tvName, tvPhone;
    private Switch switchDarkMode;
    private LinearLayout btnEditProfile, btnAddress, btnNotification, btnPayment, 
                        btnSecurity, btnLanguage, btnPrivacyPolicy, btnHelpCenter, 
                        btnInviteFriends, btnHome, btnCart, btnOrders, btnWallet, btnProfile, btnLogout;
    
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
        
        // Load user data
        loadUserData();
        
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
        btnAddress = findViewById(R.id.btnAddress);
        btnNotification = findViewById(R.id.btnNotification);
        btnPayment = findViewById(R.id.btnPayment);
        btnSecurity = findViewById(R.id.btnSecurity);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnHelpCenter = findViewById(R.id.btnHelpCenter);
        btnInviteFriends = findViewById(R.id.btnInviteFriends);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Dark mode switch
        switchDarkMode = findViewById(R.id.switchDarkMode);
        
        // Bottom navigation
        btnHome = findViewById(R.id.btnHome);
        btnCart = findViewById(R.id.btnCart);
        btnOrders = findViewById(R.id.btnOrders);
        btnWallet = findViewById(R.id.btnWallet);
        btnProfile = findViewById(R.id.btnProfile);
    }
    
    private void loadUserData() {
        // Không dùng getUserName/getUserPhone/getUserAvatar nếu không có
        String userId = sessionManager.getUserId();
        tvName.setText(userId != null ? userId : "Unknown User");
        tvPhone.setText("N/A");
    }
    
    private void setupClickListeners() {
        // Header buttons
        btnMenu.setOnClickListener(v -> onBackPressed());
        
        btnMore.setOnClickListener(v -> {
            // TODO: Show more options menu
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show();
        });
        
        // Profile section
        btnEditAvatar.setOnClickListener(v -> {
            // TODO: Open image picker for avatar
            Toast.makeText(this, "Edit avatar", Toast.LENGTH_SHORT).show();
        });
        
        // Settings buttons
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        });
        
        btnAddress.setOnClickListener(v -> {
            // TODO: Navigate to address screen
            Toast.makeText(this, "Address settings", Toast.LENGTH_SHORT).show();
        });
        
        btnNotification.setOnClickListener(v -> {
            // TODO: Navigate to notification settings
            Toast.makeText(this, "Notification settings", Toast.LENGTH_SHORT).show();
        });
        
        btnPayment.setOnClickListener(v -> {
            // TODO: Navigate to payment settings
            Toast.makeText(this, "Payment settings", Toast.LENGTH_SHORT).show();
        });
        
        btnSecurity.setOnClickListener(v -> {
            // TODO: Navigate to security settings
            Toast.makeText(this, "Security settings", Toast.LENGTH_SHORT).show();
        });
        
        btnLanguage.setOnClickListener(v -> {
            // TODO: Show language selection dialog
            Toast.makeText(this, "Language settings", Toast.LENGTH_SHORT).show();
        });
        
        btnPrivacyPolicy.setOnClickListener(v -> {
            // TODO: Open privacy policy web page
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
        });
        
        btnHelpCenter.setOnClickListener(v -> {
            // TODO: Navigate to help center
            Toast.makeText(this, "Help Center", Toast.LENGTH_SHORT).show();
        });
        
        btnInviteFriends.setOnClickListener(v -> {
            // TODO: Share app with friends
            shareApp();
        });
        
        // Dark mode switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleDarkMode(isChecked);
        });
        
        // Logout
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
        
        // Bottom navigation
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        
        btnCart.setOnClickListener(v -> {
            // TODO: Navigate to cart screen
            Toast.makeText(this, "Cart", Toast.LENGTH_SHORT).show();
        });
        
        btnOrders.setOnClickListener(v -> {
            // TODO: Navigate to orders screen
            Toast.makeText(this, "Orders", Toast.LENGTH_SHORT).show();
        });
        
        btnWallet.setOnClickListener(v -> {
            // TODO: Navigate to wallet screen
            Toast.makeText(this, "Wallet", Toast.LENGTH_SHORT).show();
        });
        
        btnProfile.setOnClickListener(v -> {
            // Already on profile screen
            Toast.makeText(this, "You are on Profile screen", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadDarkModeState() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);
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
        // Clear session
        sessionManager.clearSession();
        
        // Navigate to login screen
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onBackPressed() {
        // Navigate back to previous screen or home
        super.onBackPressed();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this screen
        loadUserData();
    }
} 