package com.example.preely.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.preely.R;
import com.example.preely.view.fragment.UserServiceManagementFragment;

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
    }
} 