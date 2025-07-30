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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.response.SkillResponse;
import com.example.preely.model.response.UserResponse;
import com.example.preely.viewmodel.CloudinaryService;
import android.widget.TextView;
import com.example.preely.model.entities.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
import android.widget.ImageButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

public class EditProfile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1001;
    private ImageView imgAvatar, btnEditAvatar;
    private EditText edtName, edtPhone, edtUsername, edtEmail, edtAddress, edtProvince, edtWard;
    private com.google.android.material.button.MaterialButton btnSaveBottom, btnCancelBottom;
    private Uri selectedImageUri;
    private CloudinaryService cloudinaryService;
    private SessionManager sessionManager;
    private UserResponse user;
    private String avatarUrl;
    private TextView tvRating;
    private boolean isUploading = false;
    private com.google.android.material.textfield.MaterialAutoCompleteTextView actvSkills;
    private com.google.android.material.chip.ChipGroup chipGroupSkills;
    private TextInputLayout tilSkills;
    private List<String> skillNames = new ArrayList<>();
    private List<SkillResponse> allSkills = new ArrayList<>();
    private List<SkillResponse> selectedSkills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSaveBottom = findViewById(R.id.btnSaveBottom);
        btnCancelBottom = findViewById(R.id.btnCancelBottom);
        edtUsername = findViewById(R.id.edtUsername);
        
        // Xử lý sự kiện nút Hủy
        btnCancelBottom.setOnClickListener(v -> finish());
        
        // Thiết lập màu chữ cho nút Save
        btnSaveBottom.setTextColor(getResources().getColor(android.R.color.white));
        edtEmail = findViewById(R.id.edtEmail);
        edtAddress = findViewById(R.id.edtAddress);
        edtProvince = findViewById(R.id.edtProvince);
        edtWard = findViewById(R.id.edtWard);
        tvRating = findViewById(R.id.tvRating);
        actvSkills = findViewById(R.id.actvSkills);
        chipGroupSkills = findViewById(R.id.chipGroupSkills);
        tilSkills = findViewById(R.id.tilSkills);
        
        // Khởi tạo AutoCompleteTextView cho kỹ năng
        initSkillsAutoComplete();
        
        // Xử lý khi chọn một kỹ năng từ danh sách
        actvSkills.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSkillName = (String) parent.getItemAtPosition(position);
            
            // Tìm và thêm kỹ năng đã chọn
            allSkills.stream()
                .filter(skill -> skill.getName().equals(selectedSkillName))
                .findFirst()
                .ifPresent(skill -> {
                    if (selectedSkills.stream().noneMatch(s -> s.getId().equals(skill.getId()))) {
                        selectedSkills.add(skill);
                        updateSelectedSkillsChips();
                        actvSkills.setText("");
                    }
                });
        });

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
        tvRating.setText("Rating: " + user.getRating());
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            loadAvatarWithRetry(avatarUrl);
        }

        cloudinaryService = new ViewModelProvider(this).get(CloudinaryService.class);

        btnEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
        });

        cloudinaryService.getUploadedUrls().observe(this, urls -> {
            if (urls != null && !urls.isEmpty()) {
                String url = urls.get(urls.size() - 1);
                avatarUrl = url;
                loadAvatarWithRetry(avatarUrl);
            }
        });

        cloudinaryService.getUploadStatus().observe(this, status -> {
            Log.d("EditProfile", "Upload status: " + status);
            if ("Uploading...".equals(status)) {
                isUploading = true;
                btnSaveBottom.setEnabled(false);
                Log.d("EditProfile", "Uploading... btnSaveBottom DISABLED");
            } else {
                isUploading = false;
                btnSaveBottom.setEnabled(true);
                Log.d("EditProfile", "Upload done or idle. btnSaveBottom ENABLED");
            }
        });

        btnSaveBottom.setOnClickListener(v -> {
            Log.d("EditProfile", "btnSave clicked. isUploading=" + isUploading);
            if (isUploading) {
                Toast.makeText(this, "Please wait for the image to finish uploading!", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String province = edtProvince.getText().toString().trim();
            String ward = edtWard.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                edtName.setError("Please enter your full name");
                return;
            }
            if (TextUtils.isEmpty(phone)) {
                edtPhone.setError("Please enter your phone number");
                return;
            }
            // Cập nhật user
            user.setFull_name(name);
            user.setPhone_number(phone);
            user.setAddress(address);
            user.setAvatar(avatarUrl);
            // Cập nhật danh sách kỹ năng đã chọn
            if (!selectedSkills.isEmpty()) {
                user.setSkills(selectedSkills);
            }

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
            // Chuyển đổi từ List<SkillResponse> sang List<DocumentReference>
            if (selectedSkills != null && !selectedSkills.isEmpty()) {
                List<DocumentReference> skillRefs = new ArrayList<>();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                
                for (SkillResponse skill : selectedSkills) {
                    if (skill.getId() != null) {
                        DocumentReference skillRef = db.collection("skills").document(skill.getId());
                        skillRefs.add(skillRef);
                    }
                }
                userEntity.setSkill_ids(skillRefs);
            }
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
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EditProfile", "Firestore update FAILED", e);
                        Toast.makeText(this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                    });
            } else {
                Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút back
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void removeSkill(SkillResponse skill) {
        selectedSkills.removeIf(s -> s.getId().equals(skill.getId()));
        updateSelectedSkillsChips();
    }

    private void initSkillsAutoComplete() {
        // Lấy danh sách kỹ năng từ Firestore
        FirebaseFirestore.getInstance().collection("skills")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    skillNames.clear();
                    allSkills.clear();
                    
                    // Lưu trữ tạm danh sách kỹ năng với ID
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String skillId = document.getId();
                        String skillName = document.getString("name");
                        if (skillName != null) {
                            skillNames.add(skillName);
                            
                            // Tạo SkillResponse từ dữ liệu lấy được
                            SkillResponse skill = new SkillResponse();
                            skill.setId(skillId);
                            skill.setName(skillName);
                            allSkills.add(skill);
                            
                            // Nếu người dùng đã có kỹ năng này, thêm vào danh sách đã chọn
                            if (user.getSkills() != null && user.getSkills().stream()
                                    .anyMatch(s -> s.getId().equals(skillId))) {
                                selectedSkills.add(skill);
                            }
                        }
                    }
                    
                    // Create adapter for AutoCompleteTextView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            skillNames
                    );
                    
                    actvSkills.setAdapter(adapter);
                    actvSkills.setThreshold(1); // Show suggestions from first character
                    
                    // Show selected skills
                    updateSelectedSkillsChips();
                }
            });
    }
    
    private void updateSelectedSkillsChips() {
        chipGroupSkills.removeAllViews();
        
        // Create a chip for each selected skill
        for (SkillResponse skill : selectedSkills) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(skill.getName());
            chip.setCloseIconVisible(true);
            
            // Handle chip removal
            chip.setOnCloseIconClickListener(v -> removeSkill(skill));
            
            chipGroupSkills.addView(chip);
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