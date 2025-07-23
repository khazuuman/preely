package com.example.preely.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.response.UserResponse;
import com.example.preely.view.fragment.MapFragment;
import com.example.preely.viewmodel.MapService;
import com.google.firebase.firestore.GeoPoint;

public class MapTestActivity extends AppCompatActivity {
    private static final String TAG = "MapTestActivity";

    private SessionManager sessionManager;
    private MapService mapService;
    private MapFragment mapFragment;
    private Button btnTogglePick, btnSaveLocation;

    private GeoPoint currentUserLocation;
    private GeoPoint pickedLocation;
    private boolean isPickEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_test);

        // Initialize components
        initializeComponents();

        // Load user location from session
        loadUserLocationFromSession();

        // Setup map fragment
        setupMapFragment();

        // Setup buttons
        setupButtons();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        mapService = new ViewModelProvider(this).get(MapService.class);

        btnTogglePick = findViewById(R.id.btn_toggle_pick);
        btnSaveLocation = findViewById(R.id.btn_save_location);
    }

    private void loadUserLocationFromSession() {
        UserResponse user = sessionManager.getUserSession();

        if (user != null) {
            Log.d(TAG, "User found: " + user.getUsername());

            // Lấy location từ UserResponse
            currentUserLocation = user.getLocation();

            if (currentUserLocation != null) {
                Log.d(TAG, "User location: " +
                        currentUserLocation.getLatitude() + ", " +
                        currentUserLocation.getLongitude());
            } else {
                Log.d(TAG, "User has no location, using default");
                // Vị trí mặc định (Hồ Chí Minh City)
                currentUserLocation = new GeoPoint(21, 106.6297);
            }
        } else {
            Log.e(TAG, "No user session found");
            // Vị trí mặc định nếu không có session
            currentUserLocation = new GeoPoint(21, 106.6297);

            Toast.makeText(this, "Không tìm thấy thông tin người dùng",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMapFragment() {
        // Tạo MapFragment với location của user, pick disabled initially
        mapFragment = MapFragment.newInstance(currentUserLocation, false);

        // Set listener để nhận location được pick
        mapFragment.setOnLocationPickedListener(newLocation -> {
            Log.d(TAG, "Location picked: " +
                    newLocation.getLatitude() + ", " + newLocation.getLongitude());

            pickedLocation = newLocation;

            Toast.makeText(this,
                    "Đã chọn vị trí: " +
                            String.format("%.6f, %.6f", newLocation.getLatitude(), newLocation.getLongitude()),
                    Toast.LENGTH_SHORT).show();
        });

        // Add fragment to container
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
    }

    private void setupButtons() {
        // Toggle pick mode button
        btnTogglePick.setOnClickListener(v -> {
            isPickEnabled = !isPickEnabled;

            // Recreate fragment with new pick mode
            mapFragment = MapFragment.newInstance(
                    pickedLocation != null ? pickedLocation : currentUserLocation,
                    isPickEnabled
            );

            mapFragment.setOnLocationPickedListener(newLocation -> {
                pickedLocation = newLocation;
                Toast.makeText(this,
                        "Đã chọn vị trí: " +
                                String.format("%.6f, %.6f", newLocation.getLatitude(), newLocation.getLongitude()),
                        Toast.LENGTH_SHORT).show();
            });

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();

            // Update button text
            btnTogglePick.setText(isPickEnabled ? "Disable Pick" : "Enable Pick");

            Toast.makeText(this,
                    isPickEnabled ? "Chế độ chọn vị trí đã bật" : "Chế độ chọn vị trí đã tắt",
                    Toast.LENGTH_SHORT).show();
        });

        // Save location button
        btnSaveLocation.setOnClickListener(v -> saveLocationToFirestore());
    }

    private void saveLocationToFirestore() {
        UserResponse user = sessionManager.getUserSession();

        if (user == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint locationToSave = pickedLocation != null ? pickedLocation : currentUserLocation;

        if (locationToSave == null) {
            Toast.makeText(this, "Không có vị trí để lưu",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Saving location for user: " + user.getId().getId());

        mapService.updateUserLocation(
                user.getId().getId(),
                locationToSave,
                new MapService.OnUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Location saved successfully");

                        runOnUiThread(() -> {
                            Toast.makeText(MapTestActivity.this,
                                    "Đã lưu vị trí thành công!",
                                    Toast.LENGTH_SHORT).show();

                            // Update session with new location
                            user.setLocation(locationToSave);
                            sessionManager.setUserSession(user);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to save location: " + error);

                        runOnUiThread(() -> {
                            Toast.makeText(MapTestActivity.this,
                                    "Lỗi lưu vị trí: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MapTestActivity destroyed");
    }
}
