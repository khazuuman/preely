package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.preely.R;
import com.example.preely.model.entities.Image;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditImageDialog extends Dialog {

    private Context context;
    private Image image;
    private OnImageDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etPostId, etLink;
    private MaterialButton btnSave, btnCancel;

    public interface OnImageDialogListener {
        void onImageSaved(Image image, boolean isEdit);
    }

    public AddEditImageDialog(@NonNull Context context, Image image, OnImageDialogListener listener) {
        super(context);
        this.context = context;
        this.image = image;
        this.listener = listener;
        this.isEditMode = image != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_image);

        initViews();
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initViews() {
        etPostId = findViewById(R.id.et_post_id);
        etLink = findViewById(R.id.et_link);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        if (isEditMode) {
            setTitle("Edit Image");
        } else {
            setTitle("Add New Image");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveImage());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        if (image != null) {
            etPostId.setText(image.getPost_id());
            etLink.setText(image.getLink());
        }
    }

    private void saveImage() {
        String postId = etPostId.getText().toString().trim();
        String link = etLink.getText().toString().trim();

        // Validation
        if (postId.isEmpty()) {
            etPostId.setError("Post ID is required");
            return;
        }

        if (link.isEmpty()) {
            etLink.setError("Image link is required");
            return;
        }

        // Basic URL validation
        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            etLink.setError("Please enter a valid URL");
            return;
        }

        // Create or update image
        Image imageToSave = isEditMode ? image : new Image();
        imageToSave.setPost_id(postId);
        imageToSave.setLink(link);

        if (listener != null) {
            listener.onImageSaved(imageToSave, isEditMode);
        }

        dismiss();
    }
} 