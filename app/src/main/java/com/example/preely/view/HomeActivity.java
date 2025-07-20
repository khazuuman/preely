package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.view.fragment.CategoryManagementFragment;
import com.example.preely.view.fragment.HomeFragment;
import com.example.preely.view.fragment.ImageManagementFragment;
import com.example.preely.view.fragment.ManagementFragment;
import com.example.preely.view.fragment.PostManagementFragment;
import com.example.preely.view.fragment.TagManagementFragment;
import com.example.preely.view.fragment.TransactionManagementFragment;
import com.example.preely.view.fragment.UserManagementFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if (!sessionManager.getLogin()) {
            sessionManager.clearSession();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        initViews();
        setupBottomNavigation();
        
        // Check if user is admin
        boolean isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        
        if (isAdmin) {
            // For admin, start with User Management tab
            bottomNavigationView.setSelectedItemId(R.id.navigation_users);
            loadFragment(new UserManagementFragment());
        } else {
            // For regular users, start with Home tab
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.navigation_users) {
                fragment = new UserManagementFragment();
            } else if (itemId == R.id.navigation_posts) {
                fragment = new PostManagementFragment();
            } else if (itemId == R.id.navigation_transactions) {
                fragment = new TransactionManagementFragment();
            } else if (itemId == R.id.navigation_management) {
                fragment = new ManagementFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}