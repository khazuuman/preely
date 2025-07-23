package com.example.preely.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.preely.R;
import com.example.preely.model.entities.Service;

public class ServiceDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvProvider, tvCategory, tvPrice, tvStatus, tvDescription;
    private RatingBar ratingBar;
    private MaterialButton btnBookService;
    private ProgressBar progressBar;
    private Service service;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);
        tvTitle = findViewById(R.id.tv_service_title);
        tvProvider = findViewById(R.id.tv_service_provider);
        tvCategory = findViewById(R.id.tv_service_category);
        tvPrice = findViewById(R.id.tv_service_price);
        tvStatus = findViewById(R.id.tv_service_status);
        tvDescription = findViewById(R.id.tv_service_description);
        ratingBar = findViewById(R.id.ratingBar);
        btnBookService = findViewById(R.id.btn_book_service);
        progressBar = findViewById(R.id.progressBar);
        loadServiceDetail();
        btnBookService.setOnClickListener(v -> {
            // TODO: Xử lý đặt dịch vụ
        });
    }

    private void loadServiceDetail() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Lấy dữ liệu service từ intent hoặc ViewModel
        // Demo dữ liệu mẫu
        service = new Service();
        service.setTitle("Service Demo");
        // service.setProviderName("Provider Demo"); // Không còn trường này trong Service
        service.setPrice(123.0);
        service.setDescription("This is a demo service description.");
        tvTitle.setText(service.getTitle());
        tvProvider.setText("Provider: Provider Demo"); // Hardcode tên provider demo
        tvCategory.setText("Category: Category Demo"); // Hardcode tên category demo
        tvPrice.setText("Price: $" + service.getPrice());
        tvStatus.setText("Status: " + service.getAvailability());
        tvDescription.setText(service.getDescription());
        ratingBar.setRating(service.getAverage_rating());
        progressBar.setVisibility(View.GONE);
    }
} 