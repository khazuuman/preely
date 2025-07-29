package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.preely.R;
import com.example.preely.view.fragment.MapFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.GeoPoint;

public class FullscreenMapPickerActivity extends AppCompatActivity {
    private static final String EXTRA_INITIAL_LOCATION_LAT = "initial_location_lat";
    private static final String EXTRA_INITIAL_LOCATION_LNG = "initial_location_lng";
    private static final String EXTRA_SELECTED_LOCATION_LAT = "selected_location_lat";
    private static final String EXTRA_SELECTED_LOCATION_LNG = "selected_location_lng";

    private MapFragment mapFragment;
    private GeoPoint selectedLocation;
    private TextView tvSelectedLocation;
    private MaterialButton btnConfirm, btnCancel;

    /**
     * ✅ Fix GeoPoint serialization - split thành lat/lng primitives
     */
    public static Intent createIntent(android.content.Context context, GeoPoint initialLocation) {
        Intent intent = new Intent(context, FullscreenMapPickerActivity.class);
        if (initialLocation != null) {
            intent.putExtra(EXTRA_INITIAL_LOCATION_LAT, initialLocation.getLatitude());
            intent.putExtra(EXTRA_INITIAL_LOCATION_LNG, initialLocation.getLongitude());
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_map_picker);

        initViews();
        setupMap();
        setupListeners();
    }

    private void initViews() {
        tvSelectedLocation = findViewById(R.id.tv_selected_location);
        btnConfirm = findViewById(R.id.btn_confirm_location);
        btnCancel = findViewById(R.id.btn_cancel_location);

        // ✅ Get initial location từ intent (lat/lng)
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_INITIAL_LOCATION_LAT) && intent.hasExtra(EXTRA_INITIAL_LOCATION_LNG)) {
            double lat = intent.getDoubleExtra(EXTRA_INITIAL_LOCATION_LAT, 0);
            double lng = intent.getDoubleExtra(EXTRA_INITIAL_LOCATION_LNG, 0);
            selectedLocation = new GeoPoint(lat, lng);
        }

        updateLocationDisplay();
    }

    private void setupMap() {
        // Tạo MapFragment fullscreen với pick enabled
        mapFragment = MapFragment.newInstance(selectedLocation, true);

        // Set listener để nhận location được pick
        mapFragment.setOnLocationPickedListener(newLocation -> {
            selectedLocation = newLocation;
            updateLocationDisplay();

            // Enable confirm button
            btnConfirm.setEnabled(true);
            btnConfirm.setAlpha(1.0f);
        });

        // Add fragment vào container
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fullscreen_map_container, mapFragment)
                .commit();
    }

    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> {
            // ✅ Return selected location (lat/lng primitives)
            Intent resultIntent = new Intent();
            if (selectedLocation != null) {
                resultIntent.putExtra(EXTRA_SELECTED_LOCATION_LAT, selectedLocation.getLatitude());
                resultIntent.putExtra(EXTRA_SELECTED_LOCATION_LNG, selectedLocation.getLongitude());
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void updateLocationDisplay() {
        if (selectedLocation != null) {
            String locationText = String.format("📍 %.6f, %.6f",
                    selectedLocation.getLatitude(), selectedLocation.getLongitude());
            tvSelectedLocation.setText(locationText);
        } else {
            tvSelectedLocation.setText("📍 Chưa chọn vị trí");
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.5f);
        }
    }

    /**
     * ✅ Fix GeoPoint deserialization
     */
    public static GeoPoint getSelectedLocation(Intent data) {
        if (data != null && data.hasExtra(EXTRA_SELECTED_LOCATION_LAT) && data.hasExtra(EXTRA_SELECTED_LOCATION_LNG)) {
            double lat = data.getDoubleExtra(EXTRA_SELECTED_LOCATION_LAT, 0);
            double lng = data.getDoubleExtra(EXTRA_SELECTED_LOCATION_LNG, 0);
            return new GeoPoint(lat, lng);
        }
        return null;
    }
}
