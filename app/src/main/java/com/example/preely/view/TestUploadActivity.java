package com.example.preely.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;
import com.example.preely.viewmodel.CloudinaryService;
import com.squareup.picasso.Picasso;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.io.File;
import java.io.FileOutputStream;

public class TestUploadActivity extends AppCompatActivity {
    
    private CloudinaryService cloudinaryService;
    private ImageView imageView;
    private Button btnPickImage;
    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvProgress;
    private TextView tvImageUrl;
    
    private Uri selectedImageUri;
    
    private static final int REQUEST_CODE_GALLERY = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied. Please grant storage permission in Settings.", Toast.LENGTH_LONG).show();
                    // Mở Settings để user có thể cấp quyền thủ công
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                }
            });
    
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Hiển thị ảnh đã chọn
                        imageView.setImageURI(selectedImageUri);
                        btnUpload.setEnabled(true);
                        tvStatus.setText("Image selected");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_upload);
        
        initViews();
        initCloudinaryService();
        setupClickListeners();
    }
    
    private void initViews() {
        imageView = findViewById(R.id.imageView);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        tvImageUrl = findViewById(R.id.tvImageUrl);
        
        // Disable upload button initially
        btnUpload.setEnabled(false);
    }
    
    private void initCloudinaryService() {
        cloudinaryService = new ViewModelProvider(this).get(CloudinaryService.class);
        
        // Observe upload status
        cloudinaryService.getUploadStatus().observe(this, status -> {
            tvStatus.setText("Status: " + status);
        });
        
        // Observe upload progress
        cloudinaryService.getUploadProgress().observe(this, progress -> {
            if (progress != null) {
                progressBar.setProgress(progress);
                tvProgress.setText("Progress: " + progress + "%");
            }
        });
        
        // Observe uploaded image URL
        cloudinaryService.getUploadedImageUrl().observe(this, imageUrl -> {
            if (imageUrl != null) {
                tvImageUrl.setText("Image URL: " + imageUrl);
                
                // Load ảnh từ Cloudinary vào ImageView
//                Picasso.get()
////                    .load(imageUrl)
////                    .placeholder(R.drawable.img)
////                    .error(R.drawable.img)
//                    .into(imageView);
                
                Toast.makeText(this, "Upload successful!", Toast.LENGTH_LONG).show();
                
                // Reset UI
                btnUpload.setEnabled(false);
                selectedImageUri = null;
            }
        });
        
        // Observe error messages
        cloudinaryService.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                tvStatus.setText("Status: Error");
            }
        });
    }
    
    private void setupClickListeners() {
        btnPickImage.setOnClickListener(v -> {
            checkPermissionAndPickImage();
        });
        
        btnUpload.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImage();
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        // Thêm button để tạo ảnh test
        Button btnCreateTestImage = findViewById(R.id.btnCreateTestImage);
        if (btnCreateTestImage != null) {
            btnCreateTestImage.setOnClickListener(v -> {
                createTestImage();
            });
        }
    }
    
    private void checkPermissionAndPickImage() {
        // Kiểm tra Android version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ sử dụng READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                pickImageFromGallery();
            }
        } else {
            // Android < 13 sử dụng READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                pickImageFromGallery();
            }
        }
    }
    
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
    
    private void uploadImage() {
        if (selectedImageUri != null) {
            // Upload ảnh lên Cloudinary
            cloudinaryService.uploadImage(selectedImageUri, "test_uploads");
            
            // Disable upload button during upload
            btnUpload.setEnabled(false);
            tvStatus.setText("Status: Uploading...");
        }
    }

    private boolean hasStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void createTestImage() {
        try {
            // Tạo ảnh test đơn giản
            Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(android.graphics.Color.RED);
            canvas.drawCircle(150, 150, 100, paint);
            
            // Lưu ảnh vào cache
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, "test_image.jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            
            // Chuyển thành Uri
            selectedImageUri = Uri.fromFile(imageFile);
            
            // Hiển thị ảnh
            imageView.setImageBitmap(bitmap);
            btnUpload.setEnabled(true);
            tvStatus.setText("Test image created");
            
            Toast.makeText(this, "Test image created successfully", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error creating test image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 