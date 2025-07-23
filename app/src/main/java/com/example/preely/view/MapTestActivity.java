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
        initializeComponents();
        loadUserLocationFromSession();
        setupMapFragment();
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

            currentUserLocation = user.getLocation();

            if (currentUserLocation != null) {
                Log.d(TAG, "User location: " +
                        currentUserLocation.getLatitude() + ", " +
                        currentUserLocation.getLongitude());
            } else {
                Log.d(TAG, "User has no location, using default");
                currentUserLocation = new GeoPoint(10.8231, 106.6297);
            }
        } else {
            Log.e(TAG, "No user session found");
            currentUserLocation = new GeoPoint(10.8231, 106.6297);

            Toast.makeText(this, "Không tìm thấy thông tin người dùng",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMapFragment() {
        mapFragment = MapFragment.newInstance(currentUserLocation, false);

        mapFragment.setOnLocationPickedListener(newLocation -> {
            Log.d(TAG, "Location picked: " +
                    newLocation.getLatitude() + ", " + newLocation.getLongitude());

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
    }

    private void setupButtons() {
        btnTogglePick.setOnClickListener(v -> {
            isPickEnabled = !isPickEnabled;

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

            btnTogglePick.setText(isPickEnabled ? "Disable Pick" : "Enable Pick");

            Toast.makeText(this,
                    isPickEnabled ? "Chế độ chọn vị trí đã bật" : "Chế độ chọn vị trí đã tắt",
                    Toast.LENGTH_SHORT).show();
        });

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
