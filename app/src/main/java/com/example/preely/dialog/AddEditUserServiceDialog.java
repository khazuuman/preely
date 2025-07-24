package com.example.preely.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import com.example.preely.R;
import com.example.preely.adapter.ImageAdapter;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.User;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.CloudinaryService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
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
    private AutoCompleteTextView actvCategory, actvProvider;
    private Spinner spinnerAvailability;
    private MaterialButton btnSave, btnCancel, btnChooseImages;
    private RecyclerView recyclerImages;
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

    public AddEditUserServiceDialog(@NonNull Context context, Service service, List<Category> categoryList, List<User> providerList, List<String> availabilityList, OnServiceDialogListener listener, ActivityResultLauncher<Intent> imagePickerLauncher, User fixedProvider) {
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
        this.fixedProvider = fixedProvider;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_service);
        Window window = getWindow();
        if (window != null) {
            window.setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
        }
        initViews();
        setupAdapters();
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etUniversity = findViewById(R.id.et_university);
        actvCategory = findViewById(R.id.actv_category);
        actvProvider = findViewById(R.id.actv_provider);
        TextInputLayout tilProvider = findViewById(R.id.til_provider);
        spinnerAvailability = findViewById(R.id.spinner_availability);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChooseImages = findViewById(R.id.btn_choose_images);
        recyclerImages = findViewById(R.id.recycler_images);
        recyclerImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(imageUrls, position -> {
            imageUrls.remove(position);
            imageAdapter.setImageList(imageUrls);
        });
        recyclerImages.setAdapter(imageAdapter);
        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        tvTitle.setText(isEditMode ? "Edit Service" : "Add New Service");
        // Ẩn và disable provider
        if (fixedProvider != null) {
            actvProvider.setText(fixedProvider.getFull_name() + " (" + fixedProvider.getEmail() + ")");
            actvProvider.setEnabled(false);
            actvProvider.setFocusable(false);
            actvProvider.setVisibility(View.GONE);
            if (tilProvider != null) tilProvider.setVisibility(View.GONE);
        }
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
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
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> dismiss());
        btnChooseImages.setOnClickListener(v -> chooseImagesFromGallery());
    }

    private void populateFields() {
        etTitle.setText(service.getTitle());
        etDescription.setText(service.getDescription());
        etPrice.setText(service.getPrice() != null ? String.valueOf(service.getPrice()) : "");
        etUniversity.setText(service.getUniversity());
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
        if (service.getImage_urls() != null) {
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
        service.setCategory_id(categoryRef);
        service.setProvider_id(providerRef);
        service.setAvailability(selectedAvailability);
        service.setImage_urls(new ArrayList<>(imageUrls));
        service.setUpdate_at(com.google.firebase.Timestamp.now());
        if (service.getId() == null) {
            service.setCreate_at(com.google.firebase.Timestamp.now());
        }
        if (listener != null) {
            listener.onServiceSaved(service, isEditMode);
        }
        dismiss();
    }
} 