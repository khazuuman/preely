package com.example.preely.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.preely.R;
import com.example.preely.view.fragment.UserServiceManagementFragment;
import android.widget.ImageButton;
import android.content.Intent;

public class UserServiceManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_service_management);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new UserServiceManagementFragment())
                .commit();
        }
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
} 