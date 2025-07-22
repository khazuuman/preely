package com.example.preely.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.preely.R;
import com.example.preely.view.TransactionActivity;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private MaterialButton btnCreateTransaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(view);
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        btnCreateTransaction = view.findViewById(R.id.btn_create_transaction);
    }

    private void setupListeners() {
        btnCreateTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TransactionActivity.class);
            startActivity(intent);
        });
    }
} 