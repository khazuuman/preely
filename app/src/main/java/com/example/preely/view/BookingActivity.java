package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.BookingRequest;
import com.example.preely.model.response.ServiceMarketDetailResponse;
import com.example.preely.util.Constraints;
import com.example.preely.util.ViewUtil;
import com.example.preely.viewmodel.BookingService;
import com.example.preely.model.entities.Transaction;
import com.example.preely.viewmodel.TransactionService;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    DatePicker startDate;
    TimePicker startTime;
    EditText etNotes, etUnitCount;
    Button btnConfirm, btnCancel;
    TextView tvErrorDay, tvErrorTime, tvErrorUnit, tvTotalPrice;
    BookingService bookingService;
    SessionManager sessionManager;
    ServiceMarketDetailResponse detailResponse;
    BookingRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);

        setupView();
        initComponent();
        setupInput();
        setupErrorTextTracking();
        setupBtn();
    }

    public void setupView() {
        startDate = findViewById(R.id.datePicker);
        startTime = findViewById(R.id.timePicker);
        etUnitCount = findViewById(R.id.etUnitCount);
        etNotes = findViewById(R.id.etNotes);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        tvErrorDay = findViewById(R.id.day_error_tv);
        tvErrorTime = findViewById(R.id.time_error_tv);
        tvErrorUnit = findViewById(R.id.unit_error_tv);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        ViewUtil.clearErrorOnTimeChanged(startTime, tvErrorTime);
        ViewUtil.clearErrorOnDateChanged(startDate, tvErrorDay);
        ViewUtil.clearErrorOnTextChanged(etUnitCount, tvErrorUnit);
    }

    public void initComponent() {
        bookingService = new BookingService();
        sessionManager = new SessionManager(this);
        detailResponse = getIntent().getParcelableExtra("serviceResponse");
        request = new BookingRequest();
    }

    @SuppressLint({"MissingInflatedId", "DefaultLocale"})
    public BookingRequest initBookingRequest() {
        DocumentReference serviceRef = null;
        if (detailResponse != null) {
            serviceRef = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.SERVICE).document(detailResponse.getId());
        }
        request.setService_id(serviceRef);
        //seeker id
        DocumentReference seekerRef = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.USERS).document(sessionManager.getUserSession().getId());
        request.setSeeker_id(seekerRef);
        //time slot
        int hour = startTime.getHour();
        int minute = startTime.getMinute();
        String timeDate = String.format("%02d:%02d", hour, minute);
        int day = startDate.getDayOfMonth();
        int month = startDate.getMonth() + 1;
        int year = startDate.getYear();
        String dayDate = String.format("%02d/%02d/%d", day, month, year);
        request.setTime_slot(timeDate + " - " + dayDate);
        //unit count
        if (!etUnitCount.getText().toString().isEmpty()) {
            request.setUnit_count(Integer.valueOf(etUnitCount.getText().toString()));
        } else {
            request.setUnit_count(0);
        }
        //total price
        assert detailResponse != null;
        if (request.getUnit_count() != null && detailResponse.getPrice() != null) {
            request.setTotal_price(detailResponse.getPrice() * request.getUnit_count());
        } else {
            request.setTotal_price((double) 0);
        }
        //note
        request.setNotes(etNotes.getText().toString());

        return request;
    }

    public void setupInput() {
        etUnitCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                assert detailResponse != null;
                if (!etUnitCount.getText().toString().isEmpty() && detailResponse.getPrice() != null) {
                    tvTotalPrice.setText("Total price: " + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(etUnitCount.getText().toString()) * detailResponse.getPrice()) + " VND");
                } else {
                    tvTotalPrice.setText("Total price: 0 VND");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setupBtn() {
        //service detail response
        btnConfirm.setOnClickListener(v -> {
            request = initBookingRequest();
            if (!bookingService.validateBooking(request, detailResponse.getAvailability())) {
                CustomToast.makeText(getApplicationContext(), "Fail to booking service", Toast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
            } else {
                try {
                    bookingService.insertBooking(request);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        btnCancel.setOnClickListener(v -> finish());
    }

    public void initTransaction() {
        TransactionService transactionService = new TransactionService();
        Transaction transaction = new Transaction();
        transaction.setService_id(detailResponse.getId());
        transaction.setRequester_id(sessionManager.getUserSession().getId());
        transaction.setAmount(request.getTotal_price());
        transaction.setStatus("Unpaid");
        transaction.setTransaction_date(Timestamp.now());
        // Nếu muốn set giver_id, có thể lấy từ provider_id của service (nếu cần)
        transactionService.saveTransaction(transaction, new TransactionService.TransactionCallback() {
            @Override
            public void onSuccess(Transaction t) {
                // Chuyển sang VNPayActivity với amount là price của service
                if (request.getTotal_price() > 0) {
                    Intent vnpayIntent = new Intent(BookingActivity.this, VNPayActivity.class);
                    vnpayIntent.putExtra("amount", request.getTotal_price());
                    vnpayIntent.putExtra("orderInfo", "Thanh toán dịch vụ: " + (detailResponse.getId() != null ? detailResponse.getId() : ""));
                    vnpayIntent.putExtra("serviceId", detailResponse.getId());
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
    }

    public void setupErrorTextTracking() {
        bookingService.getErrorMessageDay().observe(this, error -> {
            if (error != null) {
                Log.i("ERROR DAY", error);
                tvErrorDay.setText(error);
                tvErrorDay.setVisibility(View.VISIBLE);
            } else {
                tvErrorDay.setVisibility(View.GONE);
            }
        });
        bookingService.getErrorMessageTime().observe(this, error -> {
            if (error != null) {
                Log.i("ERROR TIME", error);
                tvErrorTime.setText(error);
                tvErrorTime.setVisibility(View.VISIBLE);
            } else {
                tvErrorTime.setVisibility(View.GONE);
            }
        });
        bookingService.getErrorMessageUnit().observe(this, error -> {
            if (error != null) {
                Log.i("ERROR UNIT", error);
                tvErrorUnit.setText(error);
                tvErrorUnit.setVisibility(View.VISIBLE);
            } else {
                tvErrorUnit.setVisibility(View.GONE);
            }
        });
        bookingService.getBookingResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, "Booking created successfully!", Toast.LENGTH_SHORT).show();
                initTransaction();
            } else {
                Toast.makeText(this, "Failed to create booking. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null) {
                ViewUtil.hideKeyboardIfTouchOutside(view, ev, this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}