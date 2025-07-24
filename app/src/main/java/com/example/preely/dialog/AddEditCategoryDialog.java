package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.preely.R;
import com.example.preely.model.entities.Category;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditCategoryDialog extends Dialog {

    private Context context;
    private Category category;
    private OnCategoryDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etName;
    private MaterialButton btnSave, btnCancel;

    public interface OnCategoryDialogListener {
        void onCategorySaved(Category category, boolean isEdit);
    }

    public AddEditCategoryDialog(@NonNull Context context, Category category, OnCategoryDialogListener listener) {
        super(context);
        this.context = context;
        this.category = category;
        this.listener = listener;
        this.isEditMode = category != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_category);

        initViews();
        setupSpinner();
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        TextView title = findViewById(R.id.tv_dialog_title);

        if (isEditMode) {
            title.setText("Edit Category");
        } else {
            title.setText("Add New Category");
        }
    }

    private void setupSpinner() {
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveCategory());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        if (category != null) {
            etName.setText(category.getName());
        }
    }

    private void saveCategory() {
        String name = etName.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Category name is required");
            return;
        }

        // Create or update category
        Category categoryToSave = isEditMode ? category : new Category();
        categoryToSave.setName(name);

        if (listener != null) {
            listener.onCategorySaved(categoryToSave, isEditMode);
        }

        dismiss();
    }
} 