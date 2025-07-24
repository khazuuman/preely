package com.example.preely.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.preely.R;
import com.example.preely.model.entities.User;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.Transaction;
import com.example.preely.authentication.SessionManager;
import com.example.preely.viewmodel.TransactionService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionActivity extends AppCompatActivity {
    private AutoCompleteTextView giverDropdown, serviceDropdown;
    private EditText amountInput;
    private TextView statusTv, transactionDateTv;
    private MaterialButton btnPayVNPay;
    private ProgressDialog progressDialog;
    private List<User> userList = new ArrayList<>();
    private List<Service> serviceList = new ArrayList<>();
    private List<User> filteredUserList = new ArrayList<>();
    private List<Service> filteredServiceList = new ArrayList<>();
    private ArrayAdapter<String> userAdapter;
    private ArrayAdapter<String> serviceAdapter;
    private String selectedGiverId = null;
    private String selectedServiceId = null;
    private String requesterId;
    private TransactionService transactionService;
//    private ServiceViewModel serviceViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // Initialize views
        giverDropdown = findViewById(R.id.giver_dropdown);
        serviceDropdown = findViewById(R.id.service_dropdown);
        amountInput = findViewById(R.id.amount_input);
        statusTv = findViewById(R.id.status_tv);
        transactionDateTv = findViewById(R.id.transaction_date_tv);
        btnPayVNPay = findViewById(R.id.btn_pay_vnpay);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCancelable(false);

        // Get requester ID from session
        SessionManager sessionManager = new SessionManager(this);
        requesterId = sessionManager.getUserSession().getId();
        if (requesterId == null) {
            Toast.makeText(this, getString(R.string.please_login_again), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set current date
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        transactionDateTv.setText(getString(R.string.transaction_date, today));

        // Initially disable service dropdown until giver is selected
        serviceDropdown.setEnabled(false);

        // Load users và services
        transactionService = new ViewModelProvider(this).get(TransactionService.class);
//        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        loadUsers();
//        loadServices();

        setupDropdownListeners();
        btnPayVNPay.setOnClickListener(v -> handlePayVNPay());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.hasExtra("transaction")) {
            Transaction transaction = (Transaction) data.getSerializableExtra("transaction");
            if (transaction != null) {
                statusTv.setText("Status: " + transaction.getStatus());
                Toast.makeText(this, "Payment result: " + transaction.getStatus(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadUsers() {
        progressDialog.show();
        transactionService.getAllUsers().observe(this, users -> {
            progressDialog.dismiss();
            if (users != null) {
                userList.clear();
                for (User u : users) {
                    if (u != null && u.getId() != null && !u.getId().equals(requesterId) && u.getFull_name() != null) {
                        userList.add(u);
                    }
                }
                List<String> names = new ArrayList<>();
                for (User u : userList) {
                    if (u.getFull_name() != null) {
                        names.add(u.getFull_name());
                    }
                }
                userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                giverDropdown.setAdapter(userAdapter);
                giverDropdown.setThreshold(1);
                Log.d("TransactionActivity", "Loaded users: " + names.size());
                debugAllUsers(); // Debug all users
                if (names.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_users_to_select), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.cannot_load_users), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void debugAllUsers() {
        Log.d("TransactionActivity", "=== DEBUG ALL USERS ===");
        for (int i = 0; i < userList.size(); i++) {
            User u = userList.get(i);
            if (u != null) {
                Log.d("TransactionActivity", "User " + i + ": ID=" + u.getId() + 
                      ", Name=" + u.getFull_name() + 
                      ", Email=" + u.getEmail());
            }
        }
        Log.d("TransactionActivity", "=== END DEBUG USERS ===");
    }

//    private void loadServices() {
//        progressDialog.show();
//        serviceViewModel.getServiceList().observe(this, services -> {
//            serviceList.clear();
//            if (services != null) serviceList.addAll(services);
//            serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
//                    serviceList.stream().map(Service::getTitle).collect(Collectors.toList()));
//            serviceDropdown.setAdapter(serviceAdapter);
//            progressDialog.dismiss();
//        });
//        serviceViewModel.loadServices();
//    }

    private void debugAllServices() {
        Log.d("TransactionActivity", "=== DEBUG ALL SERVICES ===");
        for (int i = 0; i < serviceList.size(); i++) {
            Service s = serviceList.get(i);
            if (s != null) {
                Log.d("TransactionActivity", "Service " + i + ": ID=" + s.getId() + 
                      ", Title=" + s.getTitle() + 
                      ", Provider_id=" + s.getProvider_id());
            }
        }
        Log.d("TransactionActivity", "=== END DEBUG SERVICES ===");
    }

    private void setupDropdownListeners() {
        giverDropdown.setOnClickListener(v -> {
            if (userAdapter != null && userAdapter.getCount() > 0) {
                giverDropdown.showDropDown();
                Log.d("TransactionActivity", "Show user dropdown: " + userAdapter.getCount());
            } else {
                Toast.makeText(this, getString(R.string.no_users_to_select), Toast.LENGTH_SHORT).show();
            }
        });
        giverDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && userAdapter != null && userAdapter.getCount() > 0) {
                giverDropdown.showDropDown();
            }
        });
        giverDropdown.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (position >= 0 && position < parent.getCount()) {
                    String name = (String) parent.getItemAtPosition(position);
                    // Use filtered list if there's a filter, otherwise use original list
                    List<User> searchList = filteredUserList.isEmpty() ? userList : filteredUserList;
                    for (User u : searchList) {
                        if (u.getFull_name() != null && u.getFull_name().equals(name)) {
                            selectedGiverId = u.getId();
                            Log.d("TransactionActivity", "Selected user: " + name + " with ID: " + selectedGiverId);
                            Log.d("TransactionActivity", "User details - Full name: " + u.getFull_name() + ", ID: " + u.getId() + ", Email: " + u.getEmail());
                            
                            // Clear previous service selection
                            selectedServiceId = null;
                            serviceDropdown.setText("");
                            
                            // Enable service dropdown first
                            serviceDropdown.setEnabled(true);
                            serviceDropdown.setHint(getString(R.string.select_service));
                            
                            // Then filter and load services
                            loadServicesForGiver(selectedGiverId);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TransactionActivity", "Error selecting user", e);
                Toast.makeText(this, getString(R.string.error_selecting_user), Toast.LENGTH_SHORT).show();
            }
        });
        giverDropdown.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUserDropdown(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        serviceDropdown.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Service dropdown clicked. Enabled: " + serviceDropdown.isEnabled() + 
                  ", Adapter count: " + (serviceAdapter != null ? serviceAdapter.getCount() : 0) + 
                  ", Selected giver: " + selectedGiverId);
            
            if (selectedGiverId == null) {
                Toast.makeText(this, getString(R.string.select_giver_first), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (serviceAdapter != null && serviceAdapter.getCount() > 0) {
                serviceDropdown.showDropDown();
                Log.d("TransactionActivity", "Show service dropdown: " + serviceAdapter.getCount());
            } else {
                Toast.makeText(this, getString(R.string.no_services_to_select), Toast.LENGTH_SHORT).show();
            }
        });
        serviceDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("TransactionActivity", "Service dropdown focus changed: " + hasFocus + 
                  ", Enabled: " + serviceDropdown.isEnabled() + 
                  ", Adapter count: " + (serviceAdapter != null ? serviceAdapter.getCount() : 0));
            
            if (hasFocus && selectedGiverId != null && serviceAdapter != null && serviceAdapter.getCount() > 0) {
                serviceDropdown.showDropDown();
            }
        });
        serviceDropdown.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (position >= 0 && position < parent.getCount()) {
                    String title = (String) parent.getItemAtPosition(position);
                    Log.d("TransactionActivity", "Selected service title: " + title);
                    
                    // Find the service in the filtered list
                    for (Service s : serviceList) {
                        if (s != null && s.getProvider_id() != null) {
                            if (s.getProvider_id().equals(selectedGiverId) 
                                && s.getTitle() != null && s.getTitle().equals(title)) {
                                selectedServiceId = s.getId();
                                Log.d("TransactionActivity", "Selected service: " + title + " with ID: " + selectedServiceId);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TransactionActivity", "Error selecting service", e);
                Toast.makeText(this, getString(R.string.error_selecting_service), Toast.LENGTH_SHORT).show();
            }
        });
        serviceDropdown.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServiceDropdown(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterUserDropdown(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            // If no query, use original list
            userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, 
                userList.stream().map(User::getFull_name).filter(Objects::nonNull).collect(Collectors.toList()));
        } else {
            // Filter based on query
            for (User u : userList) {
                if (u.getFull_name() != null && u.getFull_name().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(u);
                }
            }
            List<String> names = new ArrayList<>();
            for (User u : filteredUserList) {
                if (u.getFull_name() != null) {
                    names.add(u.getFull_name());
                }
            }
            userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        }
        giverDropdown.setAdapter(userAdapter);
        if (!query.isEmpty()) {
            giverDropdown.showDropDown();
        }
    }
    
    private void filterServiceDropdown(String query) {
        if (selectedGiverId == null) {
            Toast.makeText(this, getString(R.string.select_giver_first), Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d("TransactionActivity", "Filtering services with query: " + query + " for giver: " + selectedGiverId);
        
        List<Service> filteredServices = new ArrayList<>();
        for (Service s : serviceList) {
            if (s != null && s.getProvider_id() != null) {
                if (s.getProvider_id().equals(selectedGiverId)) {
                    if (query.isEmpty() || (s.getTitle() != null && s.getTitle().toLowerCase().contains(query.toLowerCase()))) {
                        filteredServices.add(s);
                    }
                }
            }
        }
        
        List<String> titles = new ArrayList<>();
        for (Service s : filteredServices) {
            if (s.getTitle() != null) {
                titles.add(s.getTitle());
            }
        }
        
        serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        serviceDropdown.setAdapter(serviceAdapter);
        
        if (!query.isEmpty() && !titles.isEmpty()) {
            serviceDropdown.showDropDown();
        }
        
        Log.d("TransactionActivity", "Filtered services count: " + titles.size());
    }

    @SuppressLint("StringFormatInvalid")
    private void loadServicesForGiver(String giverId) {
        Log.d("TransactionActivity", "Loading services for giver ID: " + giverId);
        Log.d("TransactionActivity", "Total services available: " + serviceList.size());
        
        // Filter services by seller_id (which is a DocumentReference path like "/user/aK9Fun5gK5DDVYYpLpiU")
        List<Service> giverServices = new ArrayList<>();
        int checkedServices = 0;
        int matchedServices = 0;
        
        for (Service s : serviceList) {
            checkedServices++;
            if (s != null && s.getProvider_id() != null) {
                Log.d("TransactionActivity", "Checking service " + checkedServices + ": " + s.getTitle() + 
                      " | Provider ID: " + s.getProvider_id() + 
                      " | Target giver: " + giverId + 
                      " | Match: " + s.getProvider_id().equals(giverId));
                
                if (s.getProvider_id().equals(giverId)) {
                    giverServices.add(s);
                    matchedServices++;
                    Log.d("TransactionActivity", "✓ MATCHED service: " + s.getTitle() + " with provider ID: " + s.getProvider_id());
                }
            } else {
                Log.e("TransactionActivity", "Service " + checkedServices + " is null or has null provider_id");
            }
        }
        
        Log.d("TransactionActivity", "Services matching summary - Checked: " + checkedServices + ", Matched: " + matchedServices);
        
        // Create titles list
        List<String> titles = new ArrayList<>();
        for (Service s : giverServices) {
            if (s.getTitle() != null) {
                titles.add(s.getTitle());
            }
        }
        
        Log.d("TransactionActivity", "Services found for giver: " + titles.size());
        
        // Update adapter and dropdown
        serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        serviceDropdown.setAdapter(serviceAdapter);
        serviceDropdown.setThreshold(1); // Show dropdown after 1 character
        
        if (titles.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_services_found_for_giver, giverId), Toast.LENGTH_SHORT).show();
            Log.d("TransactionActivity", "No services found for giver: " + giverId);
        } else {
            Log.d("TransactionActivity", "Successfully loaded " + titles.size() + " services for giver: " + giverId);
            // Show dropdown automatically
            serviceDropdown.showDropDown();
        }
    }



    @SuppressLint("StringFormatInvalid")
    private void filterServicesByGiver(String giverId) {
        Log.d("TransactionActivity", "Filtering services for giver ID: " + giverId);
        Log.d("TransactionActivity", "Total services available: " + serviceList.size());
        
        filteredServiceList.clear();
        for (Service s : serviceList) {
            // Check seller_id field
            boolean isGiverService = false;
            if (s != null && s.getProvider_id() != null) {
                if (s.getProvider_id().equals(giverId)) {
                    isGiverService = true;
                    Log.d("TransactionActivity", "Found service by provider_id: " + s.getTitle());
                }
            }
            
            if (isGiverService) {
                filteredServiceList.add(s);
                Log.d("TransactionActivity", "Found service: " + s.getTitle() + " with provider ID: " + s.getProvider_id());
            }
        }
        
        List<String> titles = new ArrayList<>();
        for (Service s : filteredServiceList) {
            if (s.getTitle() != null) {
                titles.add(s.getTitle());
            }
        }
        
        Log.d("TransactionActivity", "Filtered services count: " + titles.size());
        
        serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        serviceDropdown.setAdapter(serviceAdapter);
        
        if (titles.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_services_found_for_giver, giverId), Toast.LENGTH_SHORT).show();
            Log.d("TransactionActivity", "No services found for giver: " + giverId);
        } else {
            serviceDropdown.showDropDown();
            Log.d("TransactionActivity", "Filtered services for giver " + giverId + ": " + titles.size());
        }
    }

    private void handlePayVNPay() {
        String amountStr = amountInput.getText().toString().trim();
        if (selectedGiverId == null) {
            Toast.makeText(this, getString(R.string.please_select_giver), Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedServiceId == null) {
            Toast.makeText(this, getString(R.string.please_select_service), Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        final double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo transaction object
        Transaction transaction = new Transaction();
        transaction.setRequester_id(requesterId);
        transaction.setGiver_id(selectedGiverId);
        transaction.setService_id(selectedServiceId);
        transaction.setAmount(amount);
        transaction.setStatus("Unpaid");
        transaction.setTransaction_date(Timestamp.now());

        // Lưu transaction vào Firestore trước khi thanh toán
        transactionService.saveTransaction(transaction, new TransactionService.TransactionCallback() {
            @Override
            public void onSuccess(Transaction savedTransaction) {
                startVNPayActivity(savedTransaction, amount);
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(TransactionActivity.this, getString(R.string.create_transaction_error, error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startVNPayActivity(Transaction savedTransaction, double amount) {
        Intent intent = new Intent(TransactionActivity.this, VNPayActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("orderInfo", getString(R.string.order_info, getServiceTitleById(selectedServiceId)));
        intent.putExtra("transaction", savedTransaction);
        intent.putExtra("requesterId", requesterId);
        intent.putExtra("giverId", selectedGiverId);
        intent.putExtra("serviceId", selectedServiceId);
        startActivityForResult(intent, 1);
    }

    private String getServiceTitleById(String serviceId) {
        for (Service s : serviceList) {
            if (s.getId().equals(serviceId)) return s.getTitle();
        }
        return "";
    }
} 