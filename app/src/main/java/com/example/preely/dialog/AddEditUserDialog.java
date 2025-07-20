package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.preely.R;
import com.example.preely.model.entities.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditUserDialog extends Dialog {

    private Context context;
    private User user;
    private OnUserDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etFullName, etEmail, etPhone, etAddress;
    private MaterialButton btnSave, btnCancel;

    public interface OnUserDialogListener {
        void onUserSaved(User user, boolean isEdit);
    }

    public AddEditUserDialog(@NonNull Context context, User user, OnUserDialogListener listener) {
        super(context);
        this.context = context;
        this.user = user;
        this.listener = listener;
        this.isEditMode = user != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_user);

        initViews();
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // Update title TextView in layout
        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        if (isEditMode) {
            tvTitle.setText("Edit User");
        } else {
            tvTitle.setText("Add New User");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveUser());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        if (user != null) {
            etFullName.setText(user.getFull_name());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getPhone_number());
            etAddress.setText(user.getAddress());
        }
    }

    private void saveUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            return;
        }

        // Create or update user
        User userToSave = isEditMode ? user : new User();
        userToSave.setFull_name(fullName);
        userToSave.setEmail(email);
        userToSave.setPhone_number(phone);
        userToSave.setAddress(address);

        if (listener != null) {
            listener.onUserSaved(userToSave, isEditMode);
        }

        dismiss();
    }
} 