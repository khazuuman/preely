package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.BookingRequest;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.BookingService;
import com.example.preely.model.entities.Transaction;
import com.example.preely.viewmodel.TransactionService;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookingActivity extends AppCompatActivity {

    Spinner spinnerTimeSlot;
    EditText etNotes;
    Button btnConfirm, btnCancel;
    Double servicePrice = null;

    String[] timeSlots = {
            "07:00 - 07:30", "07:30 - 08:00",
            "08:00 - 08:30", "08:30 - 09:00",
            "09:00 - 09:30", "09:30 - 10:00",
            "10:00 - 10:30", "10:30 - 11:00",
            "11:00 - 11:30", "11:30 - 12:00",
            "12:00 - 12:30", "12:30 - 13:00",
            "13:00 - 13:30", "13:30 - 14:00",
            "14:00 - 14:30", "14:30 - 15:00",
            "15:00 - 15:30", "15:30 - 16:00",
            "16:00 - 16:30", "16:30 - 17:00",
            "17:00 - 17:30", "17:30 - 18:00",
            "18:00 - 18:30", "18:30 - 19:00",
            "19:00 - 19:30", "19:30 - 20:00",
            "20:00 - 20:30", "20:30 - 21:00"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);

        Intent intent = getIntent();
        spinnerTimeSlot = findViewById(R.id.spinnerTimeSlot);
        etNotes = findViewById(R.id.etNotes);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                timeSlots
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeSlot.setAdapter(adapter);

        BookingService bookingService = new BookingService();
        BookingRequest request = new BookingRequest();
        String serviceId = intent.getStringExtra("serviceId");
        DocumentReference serviceRef = null;
        if (serviceId != null) {
            serviceRef = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.SERVICE).document(serviceId);
        }
        request.setService_id(serviceRef);
        SessionManager sessionManager = new SessionManager(this);
        DocumentReference seekerRef = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.USERS).document(sessionManager.getUserSession().getId());
        request.setSeeker_id(seekerRef);

        // Lấy giá service từ Firestore
        if (serviceId != null) {
            FirebaseFirestore.getInstance().collection(Constraints.CollectionName.SERVICE)
                .document(serviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    com.example.preely.model.entities.Service service = documentSnapshot.toObject(com.example.preely.model.entities.Service.class);
                    if (service != null) {
                        servicePrice = service.getPrice();
                    }
                });
        }

        TransactionService transactionService = new TransactionService();

        btnConfirm.setOnClickListener(v -> {
            request.setTime_slot(spinnerTimeSlot.getSelectedItem().toString());
            request.setNotes(etNotes.getText().toString());
            try {
                // 1. Tạo record booking
                bookingService.insertBooking(request);
                // 2. Tạo record transaction (Unpaid)
                Transaction transaction = new Transaction();
                transaction.setService_id(serviceId);
                transaction.setRequester_id(sessionManager.getUserSession().getId());
                transaction.setAmount(servicePrice);
                transaction.setStatus("Unpaid");
                transaction.setTransaction_date(Timestamp.now());
                // Nếu muốn set giver_id, có thể lấy từ provider_id của service (nếu cần)
                transactionService.saveTransaction(transaction, new TransactionService.TransactionCallback() {
                    @Override
                    public void onSuccess(Transaction t) {
                        // Chuyển sang VNPayActivity với amount là price của service
                        if (servicePrice != null && servicePrice > 0) {
                            Intent vnpayIntent = new Intent(BookingActivity.this, VNPayActivity.class);
                            vnpayIntent.putExtra("amount", servicePrice);
                            vnpayIntent.putExtra("orderInfo", "Thanh toán dịch vụ: " + (serviceId != null ? serviceId : ""));
                            vnpayIntent.putExtra("serviceId", serviceId);
                            vnpayIntent.putExtra("requesterId", sessionManager.getUserSession().getId());
                            vnpayIntent.putExtra("transactionId", t.getId());
                            startActivity(vnpayIntent);
                            finish();
                        } else {
                            Toast.makeText(BookingActivity.this, "Không lấy được giá dịch vụ!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(BookingActivity.this, "Tạo transaction thất bại: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        btnCancel.setOnClickListener(v -> finish());


    }
}