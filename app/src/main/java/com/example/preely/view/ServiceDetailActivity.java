package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.preely.model.response.ServiceMarketDetailResponse;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.ServiceMarketViewModel;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;
import com.example.preely.model.entities.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ServiceDetailActivity extends AppCompatActivity {

    ServiceMarketViewModel serviceMarketViewModel;
    TextView tvTitle, tvProvider, tvCategory, tvPrice, tvStatus, tvDescription, tvRating, tvUniversity, tvAvailability;
    RatingBar ratingBar;
    Button btnBookService, btnSaved;
    ImageSlider imageSlider;
    ProgressBar progressBar;
    ServiceMarketDetailResponse response;

    @SuppressLint({"WrongViewCast", "MissingInflatedId", "SetTextI18n", "DefaultLocale"})
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
        btnBookService = findViewById(R.id.btn_booking);
        progressBar = findViewById(R.id.progressBar);
        tvRating = findViewById(R.id.tvRating);
        imageSlider = findViewById(R.id.imageSlider);
        btnSaved = findViewById(R.id.btn_saved);
        tvUniversity = findViewById(R.id.tv_service_university);
        tvAvailability = findViewById(R.id.tv_service_availability);

        serviceMarketViewModel = new ViewModelProvider(this).get(ServiceMarketViewModel.class);
        Intent intent = getIntent();
        if (intent != null) {
            String serviceId = intent.getStringExtra("serviceId");
            if (serviceId != null) {
                serviceMarketViewModel.getServiceDetail(serviceId);
                serviceMarketViewModel.getDetailResponse().observe(this, detailResponse -> {
                    if (detailResponse != null) {
                        response = detailResponse;
                        ArrayList<SlideModel> imageList = new ArrayList<>();
                        if (detailResponse.getImage_urls() != null) {
                            for (String image : detailResponse.getImage_urls()) {
                                imageList.add(new SlideModel(image, ScaleTypes.FIT));
                            }
                        }
                        if (imageList.isEmpty()) {
                            imageList.add(new SlideModel(R.drawable.img_not_found, ScaleTypes.FIT));
                        }
                        imageSlider.setImageList(imageList);
                        tvTitle.setText(detailResponse.getTitle());
                        tvProvider.setText("Provider: " + detailResponse.getProviderName());
                        tvCategory.setText("Category: " + detailResponse.getCategoryName());
                        tvPrice.setText("Price: " +NumberFormat.getNumberInstance(Locale.US).format(detailResponse.getPrice()) + " VND");
                        tvStatus.setText("Status: " + detailResponse.getStatus());
                        tvDescription.setText(detailResponse.getDescription());
                        tvRating.setText("(" + String.format("%.1f", detailResponse.getAverage_rating()) + ")");
                        ratingBar.setRating(detailResponse.getAverage_rating());
                        tvUniversity.setText("University: " + detailResponse.getUniversity());
                        try {
                            Constraints.Availability availability = Constraints.Availability.valueOf(detailResponse.getAvailability());
                            tvAvailability.setText("Availability: " + availability.getLabel());
                        } catch (IllegalArgumentException | NullPointerException e) {
                            tvAvailability.setText("Availability: N/A");
                        }
                    }
                });
            }
        }

        btnBookService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServiceDetailActivity.this, BookingActivity.class);
                intent.putExtra("serviceId", response.getId());
                startActivity(intent);
            }
        });

        btnSaved.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }


} 