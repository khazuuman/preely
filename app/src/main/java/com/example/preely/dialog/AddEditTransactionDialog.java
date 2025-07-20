package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.preely.R;
import com.example.preely.model.entities.Transaction;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditTransactionDialog extends Dialog {

    private Context context;
    private Transaction transaction;
    private OnTransactionDialogListener listener;
    private boolean isEditMode;

    private TextInputEditText etAmount, etGiverId, etRequesterId, etPostId;
    private Spinner spinnerStatus;
    private MaterialButton btnSave, btnCancel;

    public interface OnTransactionDialogListener {
        void onTransactionSaved(Transaction transaction, boolean isEdit);
    }

    public AddEditTransactionDialog(@NonNull Context context, Transaction transaction, OnTransactionDialogListener listener) {
        super(context);
        this.context = context;
        this.transaction = transaction;
        this.listener = listener;
        this.isEditMode = transaction != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_transaction);

        initViews();
        setupSpinner();
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        etGiverId = findViewById(R.id.et_giver_id);
        etRequesterId = findViewById(R.id.et_requester_id);
        etPostId = findViewById(R.id.et_post_id);
        spinnerStatus = findViewById(R.id.spinner_status);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        if (isEditMode) {
            setTitle("Edit Transaction");
        } else {
            setTitle("Add New Transaction");
        }
    }

    private void setupSpinner() {
        String[] statuses = {"Pending", "Completed", "Failed", "Cancelled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveTransaction());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        if (transaction != null) {
            if (transaction.getAmount() != null) {
                etAmount.setText(String.valueOf(transaction.getAmount()));
            }
            etGiverId.setText(transaction.getGiver_id());
            etRequesterId.setText(transaction.getRequester_id());
            etPostId.setText(transaction.getPost_id());
            
            // Set spinner selection based on status
            String status = transaction.getStatus();
            if (status != null) {
                String[] statuses = {"Pending", "Completed", "Failed", "Cancelled"};
                for (int i = 0; i < statuses.length; i++) {
                    if (statuses[i].equalsIgnoreCase(status)) {
                        spinnerStatus.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String giverId = etGiverId.getText().toString().trim();
        String requesterId = etRequesterId.getText().toString().trim();
        String postId = etPostId.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        // Validation
        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            return;
        }

        Number amount = null;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount.doubleValue() <= 0) {
                etAmount.setError("Amount must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount format");
            return;
        }

        if (giverId.isEmpty()) {
            etGiverId.setError("Giver ID is required");
            return;
        }

        if (requesterId.isEmpty()) {
            etRequesterId.setError("Requester ID is required");
            return;
        }

        if (postId.isEmpty()) {
            etPostId.setError("Post ID is required");
            return;
        }

        // Create or update transaction
        Transaction transactionToSave = isEditMode ? transaction : new Transaction();
        transactionToSave.setAmount(amount);
        transactionToSave.setGiver_id(giverId);
        transactionToSave.setRequester_id(requesterId);
        transactionToSave.setPost_id(postId);
        transactionToSave.setStatus(status);
        
        // Set current timestamp for new transactions
        if (!isEditMode) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            transactionToSave.setTransaction_date(sdf.format(new Date()));
        }

        if (listener != null) {
            listener.onTransactionSaved(transactionToSave, isEditMode);
        }

        dismiss();
    }
} 