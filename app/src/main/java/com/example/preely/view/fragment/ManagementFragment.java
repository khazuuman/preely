package com.example.preely.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.preely.R;
import com.google.android.material.button.MaterialButton;

public class ManagementFragment extends Fragment {

    private MaterialButton btnCategoryManagement;
    private MaterialButton btnTagManagement;
    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_management, container, false);
        
        initViews(view);
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        btnCategoryManagement = view.findViewById(R.id.btn_category_management);
        btnTagManagement = view.findViewById(R.id.btn_tag_management);
        fragmentManager = getParentFragmentManager();
    }

    private void setupListeners() {
        btnCategoryManagement.setOnClickListener(v -> {
            loadFragment(new CategoryManagementFragment());
        });

        btnTagManagement.setOnClickListener(v -> {
            // loadFragment(new TagManagementFragment()); // Đã xóa tag management
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
} 