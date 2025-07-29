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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.preely.R;
import com.example.preely.adapter.ImageAdapter;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.User;
import com.example.preely.util.Constraints;
import com.example.preely.view.FullscreenMapPickerActivity;
import com.example.preely.viewmodel.CloudinaryService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.List;

public class AddEditServiceDialog extends Dialog {
    private final Context context;
    private final Service service;
    private final OnServiceDialogListener listener;
    private final boolean isEditMode;
    private final List<Category> categoryList;
    private final List<User> providerList;
    private final List<String> availabilityList;

    private TextInputEditText etTitle, etDescription, etPrice, etUniversity;
    private AutoCompleteTextView actvCategory, actvProvider;
    private Spinner spinnerAvailability;
    private MaterialButton btnSave, btnCancel, btnChooseImages;
    private RecyclerView recyclerImages;
    private TextView tvLocationDisplay;

    //  Map components (thêm mới)
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
    private static final String TAG = "AddEditServiceDialog";
    private final ActivityResultLauncher<Intent> imagePickerLauncher;

    public interface OnServiceDialogListener {
        void onServiceSaved(Service service, boolean isEdit);
    }

    /**
     *  Constructor cập nhật với map picker launcher
     */
    public AddEditServiceDialog(@NonNull Context context, Service service,
                                List<Category> categoryList, List<User> providerList,
                                List<String> availabilityList, OnServiceDialogListener listener,
                                ActivityResultLauncher<Intent> imagePickerLauncher,
                                ActivityResultLauncher<Intent> mapPickerLauncher) {
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

        //  Set default/existing location
        if (isEditMode && service.getLocation() != null) {
            selectedLocation = service.getLocation();
        } else {
            selectedLocation = DEFAULT_HANOI_LOCATION;
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

            initViews();
            setupAdapters();
            setupListeners();

            //  Setup map với delay
            setOnShowListener(dialog -> {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    setupMapViewAlternative();
                }, 100);
            });

            if (isEditMode) {
                populateFields();
            } else {
                updateLocationDisplay(selectedLocation);
            }

            Log.d(TAG, "Dialog created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etUniversity = findViewById(R.id.et_university);
        actvCategory = findViewById(R.id.actv_category);
        actvProvider = findViewById(R.id.actv_provider);
        spinnerAvailability = findViewById(R.id.spinner_availability);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChooseImages = findViewById(R.id.btn_choose_images);
        recyclerImages = findViewById(R.id.recycler_images);
        tvLocationDisplay = findViewById(R.id.tv_location_display);

        //  Init map views
        btnSelectLocation = findViewById(R.id.btn_select_location);
        mapPreviewCard = findViewById(R.id.map_preview_card);
        mapPlaceholderCard = findViewById(R.id.map_placeholder_card);
        mapPreviewContainer = findViewById(R.id.map_preview_container);
        mapPreviewOverlay = findViewById(R.id.map_preview_overlay);
        tvChangeLocation = findViewById(R.id.tv_change_location);

        // Setup RecyclerView
        recyclerImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(imageUrls, position -> {
            imageUrls.remove(position);
            imageAdapter.setImageList(imageUrls);
        });
        recyclerImages.setAdapter(imageAdapter);

        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        tvTitle.setText(isEditMode ? "Edit Service" : "Add New Service");

        actvProvider.setOnClickListener(v -> actvProvider.showDropDown());
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
    }

    private void setupAdapters() {
        // Category
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) categoryNames.add(c.getName());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(categoryAdapter);

        // Provider
        List<String> providerNames = new ArrayList<>();
        for (User u : providerList) providerNames.add(u.getFull_name() + " (" + u.getEmail() + ")");
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, providerNames);
        actvProvider.setAdapter(providerAdapter);

        // Availability
        List<String> availabilityLabels = new ArrayList<>();
        for (Constraints.Availability a : Constraints.Availability.values()) {
            availabilityLabels.add(a.getLabel());
        }
        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, availabilityLabels);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(availabilityAdapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> dismiss());
        btnChooseImages.setOnClickListener(v -> chooseImagesFromGallery());

        //  Map listeners
        if (btnSelectLocation != null) {
            btnSelectLocation.setOnClickListener(v -> openFullscreenMapPicker());
        }
        if (mapPreviewOverlay != null) {
            mapPreviewOverlay.setOnClickListener(v -> openFullscreenMapPicker());
        }
        if (tvChangeLocation != null) {
            tvChangeLocation.setOnClickListener(v -> openFullscreenMapPicker());
        }
    }

    /**
     *  Setup map view alternative
     */
    private void setupMapViewAlternative() {
        if (selectedLocation != null) {
            showMapPreview(selectedLocation);
        } else {
            showMapPlaceholder();
        }
    }

    /**
     *  Mở fullscreen map picker
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
     *  Hiển thị map preview
     */
    private void showMapPreview(GeoPoint location) {
        try {
            if (location != null && mapPreviewCard != null && mapPlaceholderCard != null) {
                mapPlaceholderCard.setVisibility(View.GONE);
                mapPreviewCard.setVisibility(View.VISIBLE);

                // Setup preview map
                MapView previewMapView = new MapView(context);
                previewMapView.onCreate(null);
                previewMapView.getMapAsync(googleMap -> {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                    googleMap.getUiSettings().setAllGesturesEnabled(false);
                });

                // Add to container
                if (mapPreviewContainer != null) {
                    ((FrameLayout) mapPreviewContainer).removeAllViews();
                    ((FrameLayout) mapPreviewContainer).addView(previewMapView);
                }

                // Show overlays
                if (mapPreviewOverlay != null) mapPreviewOverlay.setVisibility(View.VISIBLE);
                if (tvChangeLocation != null) tvChangeLocation.setVisibility(View.VISIBLE);

                // Update button
                if (btnSelectLocation != null) {
                    btnSelectLocation.setText("Đổi vị trí");
                    btnSelectLocation.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_edit_location));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing map preview: " + e.getMessage());
        }
    }

    /**
     *  Hiển thị placeholder
     */
    private void showMapPlaceholder() {
        try {
            if (mapPreviewCard != null) mapPreviewCard.setVisibility(View.GONE);
            if (mapPlaceholderCard != null) mapPlaceholderCard.setVisibility(View.VISIBLE);

            if (btnSelectLocation != null) {
                btnSelectLocation.setText("Chọn vị trí");
                btnSelectLocation.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_map_expand));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing placeholder: " + e.getMessage());
        }
    }

    /**
     *  Update location display
     */
    private void updateLocationDisplay(GeoPoint location) {
        if (location != null && tvLocationDisplay != null) {
            String locationText = String.format("Vị trí: %.6f, %.6f", location.getLatitude(), location.getLongitude());
            tvLocationDisplay.setText(locationText);
        }
    }

    private void populateFields() {
        etTitle.setText(service.getTitle());
        etDescription.setText(service.getDescription());
        etPrice.setText(service.getPrice() != null ? String.valueOf(service.getPrice()) : "");
        etUniversity.setText(service.getUniversity());

        //  Update location display
        if (service.getLocation() != null) {
            selectedLocation = service.getLocation();
            updateLocationDisplay(selectedLocation);
        }

        // Category
        if (service.getCategory_id() != null) {
            String catId = service.getCategory_id().getId();
            for (Category c : categoryList) {
                if (c.getId().equals(catId)) {
                    actvCategory.setText(c.getName(), false);
                    break;
                }
            }
        }

        // Provider
        if (service.getProvider_id() != null) {
            String provId = service.getProvider_id().getId();
            for (User u : providerList) {
                String display = u.getFull_name() + " (" + u.getEmail() + ")";
                if (u.getId().equals(provId)) {
                    actvProvider.setText(display, false);
                    break;
                }
            }
        }

        // Availability
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

        // Images
        if (service.getImage_urls() != null && imageAdapter != null) {
            imageUrls.clear();
            imageUrls.addAll(service.getImage_urls());
            imageAdapter.setImageList(imageUrls);
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

        if (!uris.isEmpty()) {
            isUploadingImages = true;
            btnSave.setEnabled(false);
            cloudinaryService.clearUploadedUrls();
            cloudinaryService.uploadMultipleFiles(uris, "services");
            cloudinaryService.getUploadedUrls().observeForever(urls -> {
                if (urls != null && urls.size() >= uris.size()) {
                    imageUrls.clear();
                    imageUrls.addAll(urls);
                    imageAdapter.setImageList(imageUrls);
                    isUploadingImages = false;
                    btnSave.setEnabled(true);
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

        //  Validate location
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

        // Map category and provider
        DocumentReference categoryRef = null;
        for (Category c : categoryList) {
            if (c.getName().equals(categoryName)) {
                categoryRef = FirebaseFirestore.getInstance().collection("categories").document(c.getId());
                break;
            }
        }

        DocumentReference providerRef = null;
        String providerName = actvProvider.getText().toString().trim();
        for (User u : providerList) {
            String display = u.getFull_name() + " (" + u.getEmail() + ")";
            if (display.equals(providerName)) {
                providerRef = FirebaseFirestore.getInstance().collection("users").document(u.getId());
                break;
            }
        }

        service.setTitle(title);
        service.setDescription(description);
        service.setPrice(price);
        service.setUniversity(university);
        service.setLocation(selectedLocation); //  Use selected location from map
        service.setCategory_id(categoryRef);
        service.setProvider_id(providerRef);
        service.setAvailability(selectedAvailability);
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

    /**
     *  Handle map picker result
     */
    public void handleMapPickerResult(GeoPoint newLocation) {
        if (newLocation != null) {
            selectedLocation = newLocation;
            updateLocationDisplay(newLocation);
            showMapPreview(newLocation);
        }
    }
}
