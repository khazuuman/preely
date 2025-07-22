package com.example.preely.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.SettingItem;
import com.example.preely.view.CustomToast;
import com.example.preely.view.adapter.SettingsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivityV2 extends AppCompatActivity implements SettingsAdapter.OnSettingItemClickListener {
    
    private ImageView imgAvatar, btnEditAvatar, btnMenu, btnMore;
    private TextView tvName, tvPhone;
    private LinearLayout btnHome, btnCart, btnOrders, btnWallet, btnProfile;
    private RecyclerView recyclerViewSettings;
    
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private SettingsAdapter settingsAdapter;
    private List<SettingItem> settingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_v2);
        
        // Initialize SessionManager and SharedPreferences
        sessionManager = new SessionManager(this);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        
        // Initialize views
        initViews();
        
        // Load user data
        loadUserData();
        
        // Set up settings list
        setupSettingsList();
        
        // Set up click listeners
        setupClickListeners();
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
        
        // Settings RecyclerView
        recyclerViewSettings = findViewById(R.id.recyclerViewSettings);
        
        // Bottom navigation
        btnHome = findViewById(R.id.btnHome);
        btnCart = findViewById(R.id.btnCart);
        btnOrders = findViewById(R.id.btnOrders);
        btnWallet = findViewById(R.id.btnWallet);
        btnProfile = findViewById(R.id.btnProfile);
    }
    
    private void loadUserData() {
        // Không dùng getUserName/getUserPhone/getUserAvatar nếu không có
//        String userId = sessionManager.getUserId();
//        tvName.setText(userId != null ? userId : "Unknown User");
//        tvPhone.setText("N/A");
    }
    
    private void setupSettingsList() {
        settingsList = new ArrayList<>();
        
        // Add settings items
//        settingsList.add(new SettingItem(R.drawable.pass_icon, "Edit Profile", "", true));
//        settingsList.add(new SettingItem(R.drawable.account_icon, "Address", "", true));
//        settingsList.add(new SettingItem(R.drawable.error_icon, "Notification", "", true));
//        settingsList.add(new SettingItem(R.drawable.account_icon, "Payment", "", true));
//        settingsList.add(new SettingItem(R.drawable.success_icon, "Security", "", true));
//        settingsList.add(new SettingItem(R.drawable.account_icon, "Language", "English (US)", true));
//
//        // Dark mode switch
//        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
//        settingsList.add(new SettingItem(R.drawable.error_icon, "Dark Mode", true, isDarkMode));
//
//        // More settings
//        settingsList.add(new SettingItem(R.drawable.success_icon, "Privacy Policy", "", true));
//        settingsList.add(new SettingItem(R.drawable.error_icon, "Help Center", "", true));
//        settingsList.add(new SettingItem(R.drawable.account_icon, "Invite Friends", "", true));
//
//        // Logout
//        settingsList.add(new SettingItem(R.drawable.close_icon, "Logout", 2));
        
        // Setup RecyclerView
        recyclerViewSettings.setLayoutManager(new LinearLayoutManager(this));
        settingsAdapter = new SettingsAdapter(settingsList, this);
        recyclerViewSettings.setAdapter(settingsAdapter);
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
            Toast.makeText(this, "You are on Profile screen", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onSettingItemClick(int position, SettingItem item) {
        String title = item.getTitle();
        
        switch (title) {
            case "Edit Profile":
                Intent intent = new Intent(this, EditProfile.class);
                startActivity(intent);
                break;
            case "Address":
                Toast.makeText(this, "Address settings", Toast.LENGTH_SHORT).show();
                break;
            case "Notification":
                Toast.makeText(this, "Notification settings", Toast.LENGTH_SHORT).show();
                break;
            case "Payment":
                Toast.makeText(this, "Payment settings", Toast.LENGTH_SHORT).show();
                break;
            case "Security":
                Toast.makeText(this, "Security settings", Toast.LENGTH_SHORT).show();
                break;
            case "Language":
                Toast.makeText(this, "Language settings", Toast.LENGTH_SHORT).show();
                break;
            case "Privacy Policy":
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
                break;
            case "Help Center":
                Toast.makeText(this, "Help Center", Toast.LENGTH_SHORT).show();
                break;
            case "Invite Friends":
                shareApp();
                break;
            case "Logout":
                showLogoutDialog();
                break;
        }
    }
    
    @Override
    public void onSwitchChanged(int position, SettingItem item, boolean isChecked) {
        if ("Dark Mode".equals(item.getTitle())) {
            toggleDarkMode(isChecked);
        }
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
        super.onBackPressed();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
} 