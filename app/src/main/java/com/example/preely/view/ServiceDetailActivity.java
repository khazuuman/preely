package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.preely.model.response.ServiceMarketDetailResponse;
import com.example.preely.util.Constraints;
import com.example.preely.view.fragment.MapFragment;
import com.example.preely.viewmodel.ServiceMarketViewModel;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ServiceDetailActivity extends AppCompatActivity {
    private static final String TAG = "ServiceDetailActivity";

    ServiceMarketViewModel serviceMarketViewModel;
    TextView tvTitle, tvProvider, tvCategory, tvPrice, tvStatus, tvDescription, tvRating, tvUniversity, tvAvailability;
    RatingBar ratingBar;
    Button btnBookService, btnSaved;
    ImageSlider imageSlider;
    ProgressBar progressBar;
    ServiceMarketDetailResponse response;

    // Map components
    private FrameLayout mapContainer;
    private LinearLayout noLocationOverlay;
    private MapFragment mapFragment;

    @SuppressLint({"WrongViewCast", "MissingInflatedId", "SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        initializeViews();
        setupViewModel();
        loadServiceDetail();
    }

    private void initializeViews() {
        // Existing views
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

        // Map views
        mapContainer = findViewById(R.id.map_container);
        noLocationOverlay = findViewById(R.id.no_location_overlay);

        setupButtons();
    }

    private void setupViewModel() {
        serviceMarketViewModel = new ViewModelProvider(this).get(ServiceMarketViewModel.class);

        serviceMarketViewModel.getDetailResponse().observe(this, detailResponse -> {
            if (detailResponse != null) {
                response = detailResponse;
                populateServiceInfo(detailResponse);
                setupServiceMap(detailResponse.getLocation());
            } else {
                Log.e(TAG, "Failed to load service detail");
                // Show error state
            }
        });
    }

    private void loadServiceDetail() {
        Intent intent = getIntent();
        if (intent != null) {
            String serviceId = intent.getStringExtra("serviceId");
            if (serviceId != null) {
                Log.d(TAG, "Loading service detail for ID: " + serviceId);
                serviceMarketViewModel.getServiceDetail(serviceId);
            } else {
                Log.e(TAG, "Service ID is null");
                finish();
            }
        }
    }

    private void populateServiceInfo(ServiceMarketDetailResponse detailResponse) {
        try {
            // Image slider
            ArrayList<SlideModel> imageList = new ArrayList<>();
            if (detailResponse.getImage_urls() != null && !detailResponse.getImage_urls().isEmpty()) {
                for (String image : detailResponse.getImage_urls()) {
                    imageList.add(new SlideModel(image, ScaleTypes.FIT));
                }
            }
            if (imageList.isEmpty()) {
                imageList.add(new SlideModel(R.drawable.img_not_found, ScaleTypes.FIT));
            }
            imageSlider.setImageList(imageList);

            // Service info
            tvTitle.setText(detailResponse.getTitle() != null ? detailResponse.getTitle() : "N/A");
            tvProvider.setText("Provider: " + (detailResponse.getProviderName() != null ? detailResponse.getProviderName() : "Unknown"));
            tvCategory.setText("Category: " + (detailResponse.getCategoryName() != null ? detailResponse.getCategoryName() : "Unknown"));

            if (detailResponse.getPrice() != null) {
                tvPrice.setText("Price: " + NumberFormat.getNumberInstance(Locale.US).format(detailResponse.getPrice()) + " VND");
            } else {
                tvPrice.setText("Price: N/A");
            }

            tvStatus.setText("Status: " + (detailResponse.getStatus() != null ? detailResponse.getStatus() : "Unknown"));
            tvDescription.setText(detailResponse.getDescription() != null ? detailResponse.getDescription() : "No description available");
            tvUniversity.setText("University: " + (detailResponse.getUniversity() != null ? detailResponse.getUniversity() : "N/A"));

            // Rating
            if (detailResponse.getAverage_rating() != null) {
                tvRating.setText("(" + String.format("%.1f", detailResponse.getAverage_rating()) + ")");
                ratingBar.setRating(detailResponse.getAverage_rating());
            } else {
                tvRating.setText("(No rating)");
                ratingBar.setRating(0);
            }

            // Availability
            try {
                if (detailResponse.getAvailability() != null) {
                    Constraints.Availability availability = Constraints.Availability.valueOf(detailResponse.getAvailability());
                    tvAvailability.setText("Availability: " + availability.getLabel());
                } else {
                    tvAvailability.setText("Availability: N/A");
                }
            } catch (IllegalArgumentException e) {
                tvAvailability.setText("Availability: " + (detailResponse.getAvailability() != null ? detailResponse.getAvailability() : "N/A"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error populating service info: " + e.getMessage());
        }
    }

    /**
     * Setup map để hiển thị location của service
     */
    private void setupServiceMap(GeoPoint serviceLocation) {
        try {
            if (serviceLocation != null) {
                Log.d(TAG, "Setting up map with service location: " +
                        serviceLocation.getLatitude() + ", " + serviceLocation.getLongitude());

                // Hide no location overlay
                noLocationOverlay.setVisibility(View.GONE);

                // Create map fragment với service location (read-only mode)
                mapFragment = MapFragment.newInstanceWithTitle(serviceLocation, false, "📍 " + response.getTitle());

                // Add map fragment vào container
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.map_container, mapFragment)
                        .commitAllowingStateLoss();

                Log.d(TAG, "Map fragment added successfully");

            } else {
                Log.w(TAG, "Service location is null, showing no location overlay");

                // Show no location overlay
                noLocationOverlay.setVisibility(View.VISIBLE);

                // Remove map fragment nếu có
                if (mapFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .remove(mapFragment)
                            .commitAllowingStateLoss();
                    mapFragment = null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up service map: " + e.getMessage());

            // Fallback: show no location overlay
            noLocationOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {
        btnBookService.setOnClickListener(v -> {
            // TODO: Implement booking functionality
            Log.d(TAG, "Booking button clicked");
        });

        btnSaved.setOnClickListener(v -> {
            // TODO: Implement save service functionality
            Log.d(TAG, "Save button clicked");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ServiceDetailActivity destroyed");
    }
}
