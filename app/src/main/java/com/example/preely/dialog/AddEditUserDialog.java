package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;

import com.bumptech.glide.Glide;
import com.example.preely.R;
import com.example.preely.model.entities.User;
import com.example.preely.viewmodel.CloudinaryService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.CompletableFuture;

public class AddEditUserDialog extends Dialog {

    private Context context;
    private User user;
    private OnUserDialogListener listener;
    private boolean isEditMode;
    private ActivityResultLauncher<Intent> avatarPickerLauncher;

    private TextInputEditText etFullName, etEmail, etPhone, etAddress;
    private MaterialButton btnSave, btnCancel;
    private ImageView imgAvatarPreview;
    private MaterialButton btnChooseAvatar;
    private Uri selectedAvatarUri;
    private String uploadedAvatarUrl;
    private CloudinaryService cloudinaryService;
    private String pendingAvatarUrl; // Lưu URL upload xong
    private boolean isUploadingAvatar = false;

    public interface OnUserDialogListener {
        void onUserSaved(User user, boolean isEdit);
    }

    public AddEditUserDialog(@NonNull Context context, User user, OnUserDialogListener listener, ActivityResultLauncher<Intent> avatarPickerLauncher) {
        super(context);
        this.context = context;
        this.user = user;
        this.listener = listener;
        this.isEditMode = user != null;
        this.avatarPickerLauncher = avatarPickerLauncher;
        this.cloudinaryService = new CloudinaryService((android.app.Application) ((android.app.Activity) context).getApplication());
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
        imgAvatarPreview = findViewById(R.id.img_avatar_preview);
        btnChooseAvatar = findViewById(R.id.btn_choose_avatar);

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
        btnChooseAvatar.setOnClickListener(v -> chooseAvatarFromGallery());
    }

    private void populateFields() {
        if (user != null) {
            etFullName.setText(user.getFull_name());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getPhone_number());
            etAddress.setText(user.getAddress());
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(context).load(user.getAvatar()).placeholder(R.drawable.ic_account).into(imgAvatarPreview);
            } else {
                imgAvatarPreview.setImageResource(R.drawable.ic_account);
            }
        }
    }

    private void chooseAvatarFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (avatarPickerLauncher != null) {
            avatarPickerLauncher.launch(intent);
        }
    }

    // Call this from Activity's onActivityResult
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            selectedAvatarUri = data.getData();
            if (selectedAvatarUri != null) {
                Glide.with(context).load(selectedAvatarUri).placeholder(R.drawable.ic_account).into(imgAvatarPreview);
                // Upload lên Cloudinary
                pendingAvatarUrl = null;
                isUploadingAvatar = true;
                btnSave.setEnabled(false);
                cloudinaryService.uploadImage(selectedAvatarUri, "avatars");
                // Lắng nghe kết quả upload
                cloudinaryService.getUploadedImageUrl().observeForever(url -> {
                    if (url != null && !url.isEmpty()) {
                        pendingAvatarUrl = url;
                        isUploadingAvatar = false;
                        btnSave.setEnabled(true);
                    }
                });
            }
        }
    }

    private void saveUser() {
        if (isUploadingAvatar) {
            Toast.makeText(context, "Please wait for avatar upload to finish!", Toast.LENGTH_SHORT).show();
            return;
        }
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

        User userToSave = isEditMode ? user : new User();
        userToSave.setFull_name(fullName);
        userToSave.setEmail(email);
        userToSave.setPhone_number(phone);
        userToSave.setAddress(address);

        if (pendingAvatarUrl != null && !pendingAvatarUrl.isEmpty()) {
            userToSave.setAvatar(pendingAvatarUrl);
        }
        if (listener != null) {
            listener.onUserSaved(userToSave, isEditMode);
        }
        dismiss();
    }
} 