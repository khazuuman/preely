package com.example.preely.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;
import com.example.preely.R;
import com.example.preely.adapter.ImageAdapter;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.User;
import com.example.preely.util.Constraints;
import com.example.preely.view.FullscreenMapPickerActivity;
import com.example.preely.view.fragment.MapFragment;
import com.example.preely.viewmodel.CloudinaryService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.List;

public class AddEditUserServiceDialog extends Dialog {
    private final Context context;
    private final Service service;
    private final OnServiceDialogListener listener;
    private final boolean isEditMode;
    private final List<Category> categoryList;
    private final List<User> providerList;
    private final List<String> availabilityList;
    private final User fixedProvider;

    private TextInputEditText etTitle, etDescription, etPrice, etUniversity;
    private AutoCompleteTextView actvCategory, actvProvider, actvPriceUnit;
    private Spinner spinnerAvailability;
    private MaterialButton btnSave, btnCancel, btnChooseImages;
    private RecyclerView recyclerImages;
    private TextView tvLocationDisplay;

    // Map components
    private MapFragment mapFragment;
    private GeoPoint selectedLocation;
    private static final GeoPoint DEFAULT_HANOI_LOCATION = new GeoPoint(21.0285, 105.8542);

    private MaterialButton btnSelectLocation;
    private CardView mapPreviewCard, mapPlaceholderCard;
    private View mapPreviewOverlay;
    private TextView tvChangeLocation;
    private View mapPreviewContainer;
    private ActivityResultLauncher<Intent> mapPickerLauncher;

    private ImageAdapter imageAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private List<Uri> selectedImageUris = new ArrayList<>();
    private CloudinaryService cloudinaryService;
    private boolean isUploadingImages = false;
    private static final int REQUEST_CODE_PICK_IMAGES = 2001;
    private static final String TAG = "AddEditUserServiceDialog";
    private final ActivityResultLauncher<Intent> imagePickerLauncher;

    public interface OnServiceDialogListener {
        void onServiceSaved(Service service, boolean isEdit);
    }

    public AddEditUserServiceDialog(@NonNull Context context, Service service,
                                    List<Category> categoryList, List<User> providerList,
                                    List<String> availabilityList, OnServiceDialogListener listener,
                                    ActivityResultLauncher<Intent> imagePickerLauncher,
                                    ActivityResultLauncher<Intent> mapPickerLauncher, User fixedProvider) {
        super(context);
        this.context = context;
        this.service = service != null ? service : new Service();
        this.listener = listener;
        this.isEditMode = service != null;
        this.categoryList = categoryList;
        this.providerList = providerList;
        this.availabilityList = availabilityList;
        this.cloudinaryService = new CloudinaryService((android.app.Application) ((Activity) context).getApplication());
        this.imagePickerLauncher = imagePickerLauncher;
        this.mapPickerLauncher = mapPickerLauncher;
        this.fixedProvider = fixedProvider;

        // Set default/existing location
        if (isEditMode && service.getLocation() != null) {
            selectedLocation = service.getLocation();
        } else {
            selectedLocation = DEFAULT_HANOI_LOCATION; // Default Hà Nội cho service mới
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_add_edit_service);

            Window window = getWindow();
            if (window != null) {
                window.setLayout(
                        (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }

            //  Init views first (includes imageAdapter)
            initViews();

            //  Setup other components
            setupAdapters();
            setupListeners();

            //  Setup map với delay
            setOnShowListener(dialog -> {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    setupMapViewAlternative();
                }, 100);
            });

            //  Populate fields AFTER imageAdapter is guaranteed to be initialized
            if (isEditMode) {
                populateFields();
            } else {
                updateLocationDisplay(selectedLocation);
            }

            Log.d(TAG, "Dialog created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            //  Graceful fallback
            if (imageAdapter == null) {
                imageAdapter = new ImageAdapter(new ArrayList<>(), position -> {});
                if (recyclerImages != null) {
                    recyclerImages.setAdapter(imageAdapter);
                }
            }
        }
    }


    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etUniversity = findViewById(R.id.et_university);
        actvCategory = findViewById(R.id.actv_category);
        actvProvider = findViewById(R.id.actv_provider);
        actvPriceUnit = findViewById(R.id.actv_price_unit);
        TextInputLayout tilProvider = findViewById(R.id.til_provider);
        spinnerAvailability = findViewById(R.id.spinner_availability);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChooseImages = findViewById(R.id.btn_choose_images);
        recyclerImages = findViewById(R.id.recycler_images);
        tvLocationDisplay = findViewById(R.id.tv_location_display);

        //  Init map views (safe với null-check)
        btnSelectLocation = findViewById(R.id.btn_select_location);
        mapPreviewCard = findViewById(R.id.map_preview_card);
        mapPlaceholderCard = findViewById(R.id.map_placeholder_card);
        mapPreviewContainer = findViewById(R.id.map_preview_container);
        mapPreviewOverlay = findViewById(R.id.map_preview_overlay);
        tvChangeLocation = findViewById(R.id.tv_change_location);

        //  QUAN TRỌNG: Luôn khởi tạo imageAdapter trước khi validate
        recyclerImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(imageUrls, position -> {
            imageUrls.remove(position);
            imageAdapter.setImageList(imageUrls);
        });
        recyclerImages.setAdapter(imageAdapter);
        Log.d(TAG, "ImageAdapter initialized successfully");

        //  Validate layout sau khi init các component cần thiết
        if (!validateLayout()) {
            Log.e(TAG, "Dialog layout validation failed - some features may be disabled");
            //  Không return - vẫn tiếp tục để dialog có thể hoạt động
            hideMapSection();
        }

        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        tvTitle.setText(isEditMode ? "Edit Service" : "Add New Service");

        // Ẩn và disable provider nếu fixedProvider
        if (fixedProvider != null) {
            actvProvider.setText(fixedProvider.getFull_name() + " (" + fixedProvider.getEmail() + ")");
            actvProvider.setEnabled(false);
            actvProvider.setFocusable(false);
            actvProvider.setVisibility(View.GONE);
            if (tilProvider != null) tilProvider.setVisibility(View.GONE);
        }
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
        actvPriceUnit.setOnClickListener(v -> actvPriceUnit.showDropDown());
    }

    /**
     *  Validate layout với structure mới
     */
    private boolean validateLayout() {
        try {
            //  Check các view chính theo layout mới
            TextView locationDisplay = findViewById(R.id.tv_location_display);
            MaterialButton btnSelectLocation = findViewById(R.id.btn_select_location);
            CardView mapPreviewCard = findViewById(R.id.map_preview_card);
            CardView mapPlaceholderCard = findViewById(R.id.map_placeholder_card);

            if (locationDisplay == null) {
                Log.e(TAG, "tv_location_display not found in layout");
                return false;
            }

            if (btnSelectLocation == null) {
                Log.e(TAG, "btn_select_location not found in layout");
                return false;
            }

            if (mapPreviewCard == null) {
                Log.e(TAG, "map_preview_card not found in layout");
                return false;
            }

            if (mapPlaceholderCard == null) {
                Log.e(TAG, "map_placeholder_card not found in layout");
                return false;
            }

            Log.d(TAG, "Layout validation passed - new structure");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error validating layout: " + e.getMessage());
            return false;
        }
    }

    /**
     *  Setup MapFragment với location picking
     */
    private void setupMap() {
        try {
            // Tạo MapFragment với pick enabled = true
            mapFragment = MapFragment.newInstance(selectedLocation, true);

            // Set listener để nhận vị trí được chọn
            mapFragment.setOnLocationPickedListener(newLocation -> {
                selectedLocation = newLocation;
                updateLocationDisplay(newLocation);
                Log.d(TAG, "Location picked: " + newLocation.getLatitude() + ", " + newLocation.getLongitude());
            });

            // Add fragment vào container
            if (context instanceof FragmentActivity) {
                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.map_container, mapFragment)
                        .commitAllowingStateLoss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map: " + e.getMessage());
            // Fallback: ẩn map nếu có lỗi
            findViewById(R.id.map_container).setVisibility(View.GONE);
        }
    }


    /**
     *  Fallback: Sử dụng MapView thay vì Fragment
     */
    private void setupMapViewAlternative() {
        try {
            // Import cần thiết:
            // import com.google.android.gms.maps.MapView;
            // import com.google.android.gms.maps.OnMapReadyCallback;

            MapView mapView = new MapView(context);
            mapView.onCreate(null);
            mapView.getMapAsync(googleMap -> {
                // Setup map
                if (selectedLocation != null) {
                    showMapPreview(selectedLocation);
                } else {
                    showMapPlaceholder();
                }

                // Set click listener
                googleMap.setOnMapClickListener(latLng -> {
                    selectedLocation = new GeoPoint(latLng.latitude, latLng.longitude);
                    updateLocationDisplay(selectedLocation);

                    // Clear existing markers and add new one
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                });
            });

            // Replace container content
            FrameLayout mapContainer = findViewById(R.id.map_container);
            if (mapContainer != null) {
                mapContainer.removeAllViews();
                mapContainer.addView(mapView);
            }

            Log.d(TAG, "MapView setup completed");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up MapView: " + e.getMessage());
            hideMapSection();
        }
    }


    /**
     *  Hiển thị placeholder khi chưa chọn location
     */
    private void showMapPlaceholder() {
        mapPreviewCard.setVisibility(View.GONE);
        mapPlaceholderCard.setVisibility(View.VISIBLE);

        btnSelectLocation.setText("Chọn vị trí");
        btnSelectLocation.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_map_expand));
    }


    /**
     *  Hide map section nếu có lỗi - updated cho layout mới
     */
    private void hideMapSection() {
        try {
            //  Hide map cards safely
            if (mapPreviewCard != null) {
                mapPreviewCard.setVisibility(View.GONE);
            }

            if (mapPlaceholderCard != null) {
                mapPlaceholderCard.setVisibility(View.GONE);
            }

            //  Disable select location button
            if (btnSelectLocation != null) {
                btnSelectLocation.setEnabled(false);
                btnSelectLocation.setText("Map unavailable");
            }

            if (tvLocationDisplay != null) {
                tvLocationDisplay.setText("Vị trí: Sử dụng tọa độ mặc định (Map không khả dụng)");
            }

            // Đảm bảo vẫn có location để save
            if (selectedLocation == null) {
                selectedLocation = DEFAULT_HANOI_LOCATION;
            }

            Log.d(TAG, "Map section hidden safely, using default location");
        } catch (Exception e) {
            Log.e(TAG, "Error hiding map section: " + e.getMessage());
        }
    }


    /**
     *  Update location display text
     */
    private void updateLocationDisplay(GeoPoint location) {
        if (location != null && tvLocationDisplay != null) {
            String locationText = String.format("Vị trí: %.6f, %.6f", location.getLatitude(), location.getLongitude());
            tvLocationDisplay.setText(locationText);
        }
    }

    private void setupAdapters() {
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) categoryNames.add(c.getName());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(categoryAdapter);

        List<String> providerNames = new ArrayList<>();
        for (User u : providerList) providerNames.add(u.getFull_name() + " (" + u.getEmail() + ")");
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, providerNames);
        actvProvider.setAdapter(providerAdapter);

        List<String> availabilityLabels = new ArrayList<>();
        for (Constraints.Availability a : Constraints.Availability.values()) {
            availabilityLabels.add(a.getLabel());
        }
        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, availabilityLabels);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(availabilityAdapter);

        // Price Unit
        List<String> priceUnitLabels = new ArrayList<>();
        for (Constraints.PriceUnitType p : Constraints.PriceUnitType.values()) {
            priceUnitLabels.add(p.getLabel());
        }
        ArrayAdapter<String> priceUnitAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, priceUnitLabels);
        actvPriceUnit.setAdapter(priceUnitAdapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> dismiss());
        btnChooseImages.setOnClickListener(v -> chooseImagesFromGallery());
        btnSelectLocation.setOnClickListener(v -> openFullscreenMapPicker());
        mapPreviewOverlay.setOnClickListener(v -> openFullscreenMapPicker());
        tvChangeLocation.setOnClickListener(v -> openFullscreenMapPicker());
    }

    /**
     * Mở fullscreen map picker thay vì embedded map
     */
    private void openFullscreenMapPicker() {
        try {
            Intent intent = FullscreenMapPickerActivity.createIntent(context, selectedLocation);
            mapPickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening fullscreen map picker: " + e.getMessage());
            Toast.makeText(context, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  Hiển thị map preview sau khi chọn location
     */
    private void showMapPreview(GeoPoint location) {
        try {
            if (location != null) {
                // Hide placeholder, show preview
                mapPlaceholderCard.setVisibility(View.GONE);
                mapPreviewCard.setVisibility(View.VISIBLE);

                // Setup small preview map (read-only)
                MapView previewMapView = new MapView(context);
                previewMapView.onCreate(null);
                previewMapView.getMapAsync(googleMap -> {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Selected Location"));

                    // Disable tất cả gestures để tránh conflict
                    googleMap.getUiSettings().setAllGesturesEnabled(false);
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                });

                // Clear container và add preview
                FrameLayout container = findViewById(R.id.map_preview_container);
                container.removeAllViews();
                container.addView(previewMapView);

                // Show overlay và badge để click thay đổi
                mapPreviewOverlay.setVisibility(View.VISIBLE);
                tvChangeLocation.setVisibility(View.VISIBLE);

                // Update button text
                btnSelectLocation.setText("Đổi vị trí");
                btnSelectLocation.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_edit_location));

                Log.d(TAG, "Map preview updated successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing map preview: " + e.getMessage());
        }
    }

    /**
     *  Tạo overlay "Thay đổi vị trí" trên preview
     */
    private View createChangeLocationOverlay() {
        LinearLayout overlay = new LinearLayout(context);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(android.view.Gravity.CENTER);
        overlay.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_map_overlay));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        overlay.setOnClickListener(v -> openFullscreenMapPicker());

        TextView changeText = new TextView(context);
        changeText.setText("Thay đổi vị trí");
        changeText.setTextColor(Color.WHITE);
        changeText.setTextSize(14);
        changeText.setGravity(android.view.Gravity.CENTER);
        overlay.addView(changeText);

        return overlay;
    }

    private void populateFields() {
        etTitle.setText(service.getTitle());
        etDescription.setText(service.getDescription());
        etPrice.setText(service.getPrice() != null ? String.valueOf(service.getPrice()) : "");
        etUniversity.setText(service.getUniversity());

        // Update location display for edit mode
        if (service.getLocation() != null) {
            selectedLocation = service.getLocation();
            updateLocationDisplay(selectedLocation);
        }

        if (service.getCategory_id() != null) {
            String catId = service.getCategory_id().getId();
            for (Category c : categoryList) {
                if (c.getId().equals(catId)) {
                    actvCategory.setText(c.getName(), false);
                    break;
                }
            }
        }

        if (fixedProvider != null) {
            actvProvider.setText(fixedProvider.getFull_name() + " (" + fixedProvider.getEmail() + ")");
        } else if (service.getProvider_id() != null) {
            String provId = service.getProvider_id().getId();
            for (User u : providerList) {
                String display = u.getFull_name() + " (" + u.getEmail() + ")";
                if (u.getId().equals(provId)) {
                    actvProvider.setText(display, false);
                    break;
                }
            }
        }

        if (service.getAvailability() != null) {
            int pos = -1;
            Constraints.Availability[] availArr = Constraints.Availability.values();
            for (int i = 0; i < availArr.length; i++) {
                if (availArr[i] == service.getAvailability()) {
                    pos = i;
                    break;
                }
            }
            if (pos >= 0) spinnerAvailability.setSelection(pos);
        }

        // Price Unit
        if (service.getPrice_unit() != null) {
            actvPriceUnit.setText(service.getPrice_unit().getLabel(), false);
        }

        //  CRITICAL FIX: Null-check cho imageAdapter
        if (service.getImage_urls() != null && imageAdapter != null) {
            imageUrls.clear();
            imageUrls.addAll(service.getImage_urls());
            imageAdapter.setImageList(imageUrls);
            Log.d(TAG, "Images populated: " + imageUrls.size());
        } else if (imageAdapter == null) {
            Log.e(TAG, "ImageAdapter is null in populateFields - this should not happen");
            //  Fallback: Re-initialize imageAdapter nếu null
            imageAdapter = new ImageAdapter(imageUrls, position -> {
                imageUrls.remove(position);
                if (imageAdapter != null) {
                    imageAdapter.setImageList(imageUrls);
                }
            });
            recyclerImages.setAdapter(imageAdapter);

            // Retry populate
            if (service.getImage_urls() != null) {
                imageUrls.clear();
                imageUrls.addAll(service.getImage_urls());
                imageAdapter.setImageList(imageUrls);
            }
        }
    }


    private void chooseImagesFromGallery() {
        Log.d(TAG, "chooseImagesFromGallery: open gallery");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    public void onImagesPicked(Intent data) {
        Log.d(TAG, "onImagesPicked: data=" + (data != null));
        List<Uri> uris = new ArrayList<>();
        if (data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    uris.add(uri);
                    Log.d(TAG, "Selected image uri: " + uri);
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                uris.add(uri);
                Log.d(TAG, "Selected single image uri: " + uri);
            }
        }
        Log.d(TAG, "Total images selected: " + uris.size());
        if (!uris.isEmpty()) {
            isUploadingImages = true;
            btnSave.setEnabled(false);
            cloudinaryService.clearUploadedUrls();
            Log.d(TAG, "Uploading images to Cloudinary...");
            cloudinaryService.uploadMultipleFiles(uris, "services");
            cloudinaryService.getUploadedUrls().observeForever(urls -> {
                Log.d(TAG, "Cloudinary uploadedUrls changed: " + (urls != null ? urls.size() : 0));
                if (urls != null && urls.size() >= uris.size()) {
                    imageUrls.clear();
                    imageUrls.addAll(urls);
                    imageAdapter.setImageList(imageUrls);
                    isUploadingImages = false;
                    btnSave.setEnabled(true);
                    Log.d(TAG, "All images uploaded. imageUrls: " + imageUrls);
                }
            });
        }
    }

    private void saveService() {
        if (isUploadingImages) {
            Toast.makeText(context, "Please wait for images upload to finish!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String university = etUniversity.getText().toString().trim();
        String categoryName = actvCategory.getText().toString().trim();
        int availabilityPos = spinnerAvailability.getSelectedItemPosition();
        Constraints.Availability selectedAvailability = Constraints.Availability.values()[availabilityPos];
        
        // Get selected price unit from AutoCompleteTextView
        String selectedPriceUnitText = actvPriceUnit.getText().toString().trim();
        Constraints.PriceUnitType selectedPriceUnit = null;
        for (Constraints.PriceUnitType p : Constraints.PriceUnitType.values()) {
            if (p.getLabel().equals(selectedPriceUnitText)) {
                selectedPriceUnit = p;
                break;
            }
        }

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            return;
        }
        if (TextUtils.isEmpty(categoryName)) {
            actvCategory.setError("Category is required");
            return;
        }

        // Validate location (đã được chọn từ map)
        if (selectedLocation == null) {
            Toast.makeText(context, "Please select location on map", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUrls.isEmpty()) {
            Toast.makeText(context, "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price");
            return;
        }

        DocumentReference categoryRef = null;
        for (Category c : categoryList) {
            if (c.getName().equals(categoryName)) {
                categoryRef = FirebaseFirestore.getInstance().collection("categories").document(c.getId());
                break;
            }
        }

        DocumentReference providerRef = null;
        if (fixedProvider != null) {
            providerRef = FirebaseFirestore.getInstance().collection("users").document(fixedProvider.getId());
        }

        service.setTitle(title);
        service.setDescription(description);
        service.setPrice(price);
        service.setUniversity(university);
        service.setLocation(selectedLocation); // Sử dụng location từ map
        service.setCategory_id(categoryRef);
        service.setProvider_id(providerRef);
        service.setAvailability(selectedAvailability);
        service.setPrice_unit(selectedPriceUnit);
        service.setImage_urls(new ArrayList<>(imageUrls));
        service.setUpdate_at(Timestamp.now());
        if (service.getId() == null) {
            service.setCreate_at(Timestamp.now());
        }

        if (listener != null) {
            listener.onServiceSaved(service, isEditMode);
        }
        dismiss();
    }

    public void handleMapPickerResult(GeoPoint newLocation) {
        if (newLocation != null) {
            selectedLocation = newLocation;
            updateLocationDisplay(newLocation);
            showMapPreview(newLocation);
        }
    }
}
