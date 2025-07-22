package com.example.preely.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.response.UserResponse;
import com.example.preely.util.DataUtil;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private Button btnChangePassword;
    private SessionManager sessionManager;
    private UserResponse user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Thêm xử lý nút back
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        sessionManager = new SessionManager(this);
        user = sessionManager.getUserSession();

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = edtCurrentPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = (user.getId() != null) ? user.getId().getId() : null;
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String encodePassword = documentSnapshot.getString("encode_password");
                            if (!DataUtil.checkPassword(currentPassword, encodePassword)) {
                                Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (!newPassword.equals(confirmPassword)) {
                                Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (!DataUtil.isValidPassword(newPassword)) {
                                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 8 ký tự, 1 chữ hoa, 1 số, 1 ký tự đặc biệt", Toast.LENGTH_LONG).show();
                                return;
                            }
                            // Hash mật khẩu mới
                            String newHashedPassword = DataUtil.hashPassword(newPassword);
                            // Update Firestore
                            FirebaseFirestore.getInstance()
                                .collection("user")
                                .document(userId)
                                .update("encode_password", newHashedPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Đổi mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
                                });
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                Toast.makeText(this, "Không tìm thấy ID người dùng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 