package com.example.preely.view;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.preely.R;
import com.example.preely.view.fragment.ManagementFragment;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.preely.view.fragment.UserManagementFragment;
import com.example.preely.view.fragment.CategoryManagementFragment;
import com.example.preely.view.fragment.TransactionManagementFragment;
import android.app.AlertDialog;
import com.example.preely.view.fragment.ServiceManagementFragment;
import com.example.preely.view.fragment.SkillsManagementFragment;

public class ManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_management);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
        // Mặc định mở quản lý user
        loadFragment(new UserManagementFragment());
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_users) {
            loadFragment(new UserManagementFragment());
            return true;
        } else if (id == R.id.navigation_services) {
            loadFragment(new ServiceManagementFragment());
            return true;
        } else if (id == R.id.navigation_categories) {
            loadFragment(new CategoryManagementFragment());
            return true;
        } else if (id == R.id.navigation_more) {
            showMoreDialog();
            return false;
        }
        return false;
    }

    private void showMoreDialog() {
        String[] options = {"Transactions", "Skills"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("More Management")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        loadFragment(new TransactionManagementFragment());
                    } else if (which == 1) {
                        loadFragment(new SkillsManagementFragment());
                    }
                })
                .show();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.management_fragment_container, fragment);
        transaction.commit();
    }
} 