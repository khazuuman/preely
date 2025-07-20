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

public class AddEditPostDialog extends Dialog {

    private Context context;
    private Post post;
    private OnPostDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etTitle, etDescription, etPrice;
    private MaterialButton btnSave, btnCancel;

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

    private void savePost() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }

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

        // Create or update post
        Post postToSave = isEditMode ? post : new Post();
        postToSave.setTitle(title);
        postToSave.setDescription(description);
        postToSave.setPrice(price);

        if (listener != null) {
            listener.onPostSaved(postToSave, isEditMode);
        }

        dismiss();
    }
} 