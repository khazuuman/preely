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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookingActivity extends AppCompatActivity {

    Spinner spinnerTimeSlot;
    EditText etNotes;
    Button btnSubmit;

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
        btnSubmit = findViewById(R.id.btnSubmit);

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


        btnSubmit.setOnClickListener(v -> {
            request.setTime_slot(spinnerTimeSlot.getSelectedItem().toString());
            request.setNotes(etNotes.getText().toString());

            try {
                bookingService.insertBooking(request);
                bookingService.getBookingResult().observe(this, result -> {
                    if (result) {
                        CustomToast.makeText(this, "Booking successful", Toast.LENGTH_SHORT).show();
                    } else {
                        CustomToast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }

            // redirect to transaction
            Intent intent1 = new Intent(this, TransactionActivity.class);
            startActivity(intent1);
            finish();
        });


    }
}