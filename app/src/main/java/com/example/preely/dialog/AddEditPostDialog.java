package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.example.preely.R;
import com.example.preely.model.entities.Post;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.preely.viewmodel.CloudinaryService;
import android.widget.Toast;
import com.example.preely.dialog.ImagePreviewAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class AddEditPostDialog extends Dialog {

    private Context context;
    private LifecycleOwner lifecycleOwner;
    private Post post;
    private OnPostDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etTitle, etDescription, etPrice;
    private MaterialButton btnSave, btnCancel;
    private RecyclerView rvImagePreview;
    private MaterialButton btnChooseImages;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private CloudinaryService cloudinaryService;
    private List<String> uploadedImageUrls = new ArrayList<>();
    private boolean isUploadingImages = false;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public interface OnPostDialogListener {
        void onPostSaved(Post post, boolean isEdit);
    }

    public AddEditPostDialog(@NonNull Context context, @NonNull LifecycleOwner lifecycleOwner, Post post, OnPostDialogListener listener, ActivityResultLauncher<Intent> imagePickerLauncher) {
        super(context);
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.post = post;
        this.listener = listener;
        this.isEditMode = post != null;
        this.cloudinaryService = new CloudinaryService((android.app.Application) ((android.app.Activity) context).getApplication());
        this.imagePickerLauncher = imagePickerLauncher;
    }

    public void setOnPostDialogListener(OnPostDialogListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_post);

        initViews();
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        rvImagePreview = findViewById(R.id.rv_image_preview);
        btnChooseImages = findViewById(R.id.btn_choose_images);
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris);
        rvImagePreview.setAdapter(imagePreviewAdapter);
        rvImagePreview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));

        // Update title TextView in layout
        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        if (isEditMode) {
            tvTitle.setText("Edit Post");
        } else {
            tvTitle.setText("Add New Post");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> savePost());
        btnCancel.setOnClickListener(v -> dismiss());
        btnChooseImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });
    }

    private void populateFields() {
        if (post != null) {
            etTitle.setText(post.getTitle());
            etDescription.setText(post.getDescription());
            if (post.getPrice() != null) {
                etPrice.setText(String.valueOf(post.getPrice()));
            }
        }
    }

    // Call this from Activity's onActivityResult
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            selectedImageUris.clear();
            uploadedImageUrls.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }
            imagePreviewAdapter.notifyDataSetChanged();
            // Bắt đầu upload ngay sau khi chọn xong
            if (!selectedImageUris.isEmpty()) {
                isUploadingImages = true;
                btnSave.setEnabled(false);
                uploadAllImagesToCloudinary();
            }
        }
    }

    private void uploadAllImagesToCloudinary() {
        uploadedImageUrls.clear();
        cloudinaryService.clearUploadedUrls();
        cloudinaryService.uploadMultipleImages(selectedImageUris, "posts");
        cloudinaryService.getUploadedUrls().observe(lifecycleOwner, urls -> {
            if (urls != null && urls.size() == selectedImageUris.size()) {
                uploadedImageUrls.clear();
                uploadedImageUrls.addAll(urls);
                isUploadingImages = false;
                btnSave.setEnabled(true);
            }
        });
    }

    private void savePost() {
        if (isUploadingImages) {
            Toast.makeText(context, "Please wait for images upload to finish!", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        Double price = null;
        if (!priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) {
                    etPrice.setError("Price must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price format");
                return;
            }
        }
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }
        Post postToSave = isEditMode ? post : new Post();
        postToSave.setTitle(title);
        postToSave.setDescription(description);
        postToSave.setPrice(price);
        postToSave.setImages(new ArrayList<>(uploadedImageUrls));
        if (listener != null) {
            listener.onPostSaved(postToSave, isEditMode);
        }
        dismiss();
    }

    public List<Uri> getSelectedImageUris() {
        return selectedImageUris;
    }

    public List<String> getUploadedImageUrls() {
        return uploadedImageUrls;
    }
} 