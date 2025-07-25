package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.SettingsAdapter;
import com.example.preely.model.SettingItem;
import com.example.preely.authentication.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.OnSettingItemClickListener {
    private RecyclerView recyclerViewSettings;
    private SettingsAdapter settingsAdapter;
    private List<SettingItem> settingsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.menu_settings);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewSettings = findViewById(R.id.recyclerViewSettings);
        recyclerViewSettings.setLayoutManager(new LinearLayoutManager(this));

        settingsList = getSettingsList();
        settingsAdapter = new SettingsAdapter(settingsList, this);
        recyclerViewSettings.setAdapter(settingsAdapter);
    }

    private List<SettingItem> getSettingsList() {
        List<SettingItem> list = new ArrayList<>();
        // Edit Profile
        list.add(new SettingItem(R.drawable.ic_edit, getString(R.string.edit_profile), "", true));
        // Change Password
        list.add(new SettingItem(R.drawable.ic_pass, getString(R.string.change_password), "", true));
        // Notification (switch)
        list.add(new SettingItem(R.drawable.ic_notification, getString(R.string.notification), true, true));
        // Privacy Policy
        list.add(new SettingItem(R.drawable.ic_pass, getString(R.string.privacy_policy), "", true));
        // Logout
        list.add(new SettingItem(R.drawable.ic_back, getString(R.string.logout), 2));
        return list;
    }

    @Override
    public void onSettingItemClick(int position, SettingItem item) {
        switch (item.getTitle()) {
            case "Edit Profile":
            case "Chỉnh sửa hồ sơ":
                startActivity(new Intent(this, EditProfile.class));
                break;
            case "Change Password":
            case "Đổi mật khẩu":
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
            case "Privacy Policy":
            case "Chính sách riêng tư":
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
                break;
            case "Logout":
            case "Đăng xuất":
                // Clear session và điều hướng về Login
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.clearSession();
                Intent intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSwitchChanged(int position, SettingItem item, boolean isChecked) {
        // TODO: Lưu trạng thái notification nếu cần
        Toast.makeText(this, getString(R.string.notification) + ": " + (isChecked ? "On" : "Off"), Toast.LENGTH_SHORT).show();
    }
} 