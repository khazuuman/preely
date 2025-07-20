package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.preely.R;
import com.example.preely.model.entities.Tag;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditTagDialog extends Dialog {

    private Context context;
    private Tag tag;
    private OnTagDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etName;
    private MaterialButton btnSave, btnCancel;

    public interface OnTagDialogListener {
        void onTagSaved(Tag tag, boolean isEdit);
    }

    public AddEditTagDialog(@NonNull Context context, Tag tag, OnTagDialogListener listener) {
        super(context);
        this.context = context;
        this.tag = tag;
        this.listener = listener;
        this.isEditMode = tag != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_tag);

        initViews();
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        if (isEditMode) {
            setTitle("Edit Tag");
        } else {
            setTitle("Add New Tag");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveTag());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        if (tag != null) {
            etName.setText(tag.getName());
        }
    }

    private void saveTag() {
        String name = etName.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Tag name is required");
            return;
        }

        // Create or update tag
        Tag tagToSave = isEditMode ? tag : new Tag();
        tagToSave.setName(name);

        if (listener != null) {
            listener.onTagSaved(tagToSave, isEditMode);
        }

        dismiss();
    }
} 