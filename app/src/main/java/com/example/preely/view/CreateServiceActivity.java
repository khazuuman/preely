package com.example.preely.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.preely.util.Constraints;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.preely.R;
import com.example.preely.model.entities.Service;

public class CreateServiceActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etDescription, etPrice;
    private AutoCompleteTextView actvCategory, actvProvider;
    private Spinner spinnerStatus;
    private MaterialButton btnSave, btnCancel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_service);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        actvCategory = findViewById(R.id.actv_category);
        actvProvider = findViewById(R.id.actv_provider);
        spinnerStatus = findViewById(R.id.spinner_status);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progressBar);
        setupDropdowns();
        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // TODO: Load category/provider/status từ ViewModel/Repository
        String[] categories = {"Tutoring", "Design", "Tech Support"};
        String[] providers = {"John Doe", "Jane Smith", "Alice"};
        String[] statuses = {"Active", "Inactive"};
        actvCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories));
        actvProvider.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, providers));
        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses));
    }

    private void saveService() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Validate và lưu service qua ViewModel/Repository
        Service service = new Service();
        service.setTitle(etTitle.getText() != null ? etTitle.getText().toString() : "");
        service.setDescription(etDescription.getText() != null ? etDescription.getText().toString() : "");
        try {
            service.setPrice(Double.parseDouble(etPrice.getText() != null ? etPrice.getText().toString() : "0"));
        } catch (Exception e) {
            service.setPrice(0.0);
        }
        service.setCategory_id(null);
        service.setProvider_id(null);
        Object selectedItem = spinnerStatus.getSelectedItem();
        Constraints.Availability availability = null;

        if (selectedItem != null && !selectedItem.toString().isEmpty()) {
            try {
                availability = Constraints.Availability.valueOf(selectedItem.toString());
            } catch (IllegalArgumentException e) {
            }
        }

        service.setAvailability(availability);
        progressBar.setVisibility(View.GONE);
        finish();
    }
} 