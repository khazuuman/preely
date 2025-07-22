package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.example.preely.util.ImageUploadUtil;
import android.widget.Toast;
import com.example.preely.dialog.ImagePreviewAdapter;

public class AddEditPostDialog extends Dialog {

    private Context context;
    private Post post;
    private OnPostDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etTitle, etDescription, etPrice;
    private MaterialButton btnSave, btnCancel;
    private RecyclerView rvImagePreview;
    private MaterialButton btnChooseImages;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private ImageUploadUtil imageUploadUtil;

    public interface OnPostDialogListener {
        void onPostSaved(Post post, boolean isEdit);
    }

    public AddEditPostDialog(@NonNull Context context, Post post, OnPostDialogListener listener) {
        super(context);
        this.context = context;
        this.post = post;
        this.listener = listener;
        this.isEditMode = post != null;
    }

    public void setOnPostDialogListener(OnPostDialogListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_post);

        imageUploadUtil = new ImageUploadUtil(context);
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
        btnChooseImages.setOnClickListener(v -> chooseImagesFromGallery());
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

    private void chooseImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        ((android.app.Activity) context).startActivityForResult(Intent.createChooser(intent, "Select Images"), 2001);
    }

    // Call this from Activity's onActivityResult
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2001 && resultCode == android.app.Activity.RESULT_OK) {
            selectedImageUris.clear();
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
        }
    }

    private void savePost() {
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
        if (listener != null) {
            listener.onPostSaved(postToSave, isEditMode);
        }
        // Nếu là thêm mới, upload ảnh sau khi post đã có id
        if (!selectedImageUris.isEmpty() && !isEditMode) {
            // Cần truyền postId sau khi post được lưu thành công
            // Gợi ý: listener.onPostSaved cần trả về postId mới để upload ảnh
            // Ở đây chỉ demo upload, bạn cần chỉnh lại luồng thực tế cho đúng postId
            // imageUploadUtil.uploadMultipleImagesForPost(selectedImageUris.toArray(new Uri[0]), postId, callback);
        }
        dismiss();
    }

    public List<Uri> getSelectedImageUris() {
        return selectedImageUris;
    }
} 