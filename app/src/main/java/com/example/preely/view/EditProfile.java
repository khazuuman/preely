package com.example.preely.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.preely.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.response.UserResponse;
import com.example.preely.viewmodel.CloudinaryService;
import android.widget.TextView;
import com.example.preely.model.entities.User;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
import android.widget.ImageButton;

public class EditProfile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1001;
    private ImageView imgAvatar, btnEditAvatar;
    private EditText edtName, edtPhone, edtUsername, edtEmail, edtAddress, edtProvince, edtWard;
    private Button btnSave;
    private Uri selectedImageUri;
    private CloudinaryService cloudinaryService;
    private SessionManager sessionManager;
    private UserResponse user;
    private String avatarUrl;
    private TextView tvRating;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtAddress = findViewById(R.id.edtAddress);
        edtProvince = findViewById(R.id.edtProvince);
        edtWard = findViewById(R.id.edtWard);
        tvRating = findViewById(R.id.tvRating);

        sessionManager = new SessionManager(this);
        user = sessionManager.getUserSession();
        avatarUrl = user.getAvatar();
        edtName.setText(TextUtils.isEmpty(user.getFull_name()) ? getString(R.string.hint_full_name) : user.getFull_name());
        edtUsername.setText(TextUtils.isEmpty(user.getUsername()) ? getString(R.string.hint_username) : user.getUsername());
        edtEmail.setText(TextUtils.isEmpty(user.getEmail()) ? getString(R.string.hint_email) : user.getEmail());
        edtPhone.setText(TextUtils.isEmpty(user.getPhone_number()) ? getString(R.string.hint_phone) : user.getPhone_number());
        edtAddress.setText(TextUtils.isEmpty(user.getAddress()) ? getString(R.string.hint_address) : user.getAddress());
        edtProvince.setText(TextUtils.isEmpty(user.getProvince()) ? getString(R.string.hint_province) : user.getProvince());
        edtWard.setText(TextUtils.isEmpty(user.getWard()) ? getString(R.string.hint_ward) : user.getWard());
        tvRating.setText("Đánh giá: " + user.getRating());
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            loadAvatarWithRetry(avatarUrl);
        }

        cloudinaryService = new ViewModelProvider(this).get(CloudinaryService.class);

        btnEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
        });

        cloudinaryService.getUploadedUrls().observe(this, urls -> {
            if (urls != null && !urls.isEmpty()) {
                String url = urls.get(urls.size() - 1);
                avatarUrl = url;
                loadAvatarWithRetry(avatarUrl);
            }
        });

        cloudinaryService.getUploadStatus().observe(this, status -> {
            if ("Uploading...".equals(status)) {
                isUploading = true;
                btnSave.setEnabled(false);
                // Hiện loading nếu muốn
            }
        });

        btnSave.setOnClickListener(v -> {
            if (isUploading) {
                Toast.makeText(this, "Vui lòng chờ ảnh tải xong!", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String province = edtProvince.getText().toString().trim();
            String ward = edtWard.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                edtName.setError("Vui lòng nhập họ tên");
                return;
            }
            if (TextUtils.isEmpty(phone)) {
                edtPhone.setError("Vui lòng nhập số điện thoại");
                return;
            }
            // Cập nhật user
            user.setFull_name(name);
            user.setPhone_number(phone);
            user.setAddress(address);
            user.setAvatar(avatarUrl);
            // Tạo object User (entities) để update Firestore
            User userEntity = new User();
            userEntity.setId(user.getId());
            userEntity.setUsername(user.getUsername());
            userEntity.setFull_name(name);
            userEntity.setPhone_number(phone);
            userEntity.setAddress(address);
            userEntity.setAvatar(avatarUrl);
            userEntity.setEmail(user.getEmail());
            userEntity.set_active(true);
            userEntity.setRating(user.getRating());
            // ... map các trường khác nếu cần
            String userId = user.getId() != null ? user.getId() : null;
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .set(userEntity)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EditProfile", "Firestore update SUCCESS");
                        sessionManager.setUserSession(user);
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EditProfile", "Firestore update FAILED", e);
                        Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    });
            } else {
                Toast.makeText(this, "Không tìm thấy ID người dùng!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút back
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadAvatarWithRetry(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .timeout(3000) // 3 seconds timeout
                .into(imgAvatar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).placeholder(R.drawable.ic_image).into(imgAvatar);
            // Upload lên Cloudinary
            cloudinaryService.uploadFile(selectedImageUri, "avatars");
        }
    }
}