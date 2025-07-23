package com.example.preely.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.entities.Post;
import com.example.preely.viewmodel.CloudinaryService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddEditPostDialogFragment extends DialogFragment {
    private static final String ARG_POST_ID = "arg_post_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_PRICE = "arg_price";
    private static final String ARG_IMAGES = "arg_images";
    private String postId;
    private String title;
    private String description;
    private Double price;
    private ArrayList<String> images;
    private boolean isEditMode;
    private OnPostDialogListener listener;

    private TextInputEditText etTitle, etDescription, etPrice;
    private MaterialButton btnSave, btnCancel, btnChooseImages;
    private RecyclerView rvImagePreview;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private CloudinaryService cloudinaryService;
    private List<String> uploadedImageUrls = new ArrayList<>();
    private boolean isUploadingImages = false;
    private Observer<List<String>> uploadUrlsObserver;
    private Observer<String> uploadErrorObserver;

    public interface OnPostDialogListener {
        void onPostSaved(Post post, boolean isEdit);
    }

    public static AddEditPostDialogFragment newInstance(String postId, String title, String description, Double price, ArrayList<String> images) {
        AddEditPostDialogFragment fragment = new AddEditPostDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        if (price != null) args.putDouble(ARG_PRICE, price);
        if (images != null) args.putStringArrayList(ARG_IMAGES, images);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPostDialogListener(OnPostDialogListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_edit_post, container, false);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            price = getArguments().containsKey(ARG_PRICE) ? getArguments().getDouble(ARG_PRICE) : null;
            images = getArguments().getStringArrayList(ARG_IMAGES);
            isEditMode = postId != null;
        }
        cloudinaryService = new CloudinaryService(requireActivity().getApplication());
        initViews(view);
        setupListeners();
        if (isEditMode) {
            populateFields();
        }
        return view;
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        rvImagePreview = view.findViewById(R.id.rv_image_preview);
        btnChooseImages = view.findViewById(R.id.btn_choose_images);
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris);
        rvImagePreview.setAdapter(imagePreviewAdapter);
        rvImagePreview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        tvTitle.setText(isEditMode ? "Edit Post" : "Add New Post");
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> savePost());
        btnCancel.setOnClickListener(v -> dismiss());
        btnChooseImages.setOnClickListener(v -> chooseImagesFromGallery());
    }

    private void populateFields() {
        etTitle.setText(title != null ? title : "");
        etDescription.setText(description != null ? description : "");
        if (price != null) {
            etPrice.setText(String.valueOf(price));
        }
        // Preview các ảnh cũ nếu có và chưa chọn ảnh mới
        if ((selectedImageUris == null || selectedImageUris.isEmpty()) && images != null && !images.isEmpty()) {
            selectedImageUris.clear();
            for (String url : images) {
                // Dùng Uri.parse để preview ảnh cũ dạng URL
                selectedImageUris.add(Uri.parse(url));
            }
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }

    private void chooseImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 2001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            List<Uri> uris = new ArrayList<>();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    uris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                uris.add(data.getData());
            }
            onImagesSelected(uris);
        }
    }

    public void onImagesSelected(List<Uri> uris) {
        Log.d("AddEditPostDialogFragment", "onImagesSelected called, uris size: " + (uris == null ? 0 : uris.size()));
        selectedImageUris.clear(); // Ghi đè ảnh cũ
        uploadedImageUrls.clear();
        if (uris != null) {
            selectedImageUris.addAll(uris);
        }
        imagePreviewAdapter.notifyDataSetChanged();
        if (!selectedImageUris.isEmpty()) {
            isUploadingImages = true;
            btnSave.setEnabled(false);
            if (uploadUrlsObserver != null) {
                cloudinaryService.getUploadedUrls().removeObserver(uploadUrlsObserver);
            }
            if (uploadErrorObserver != null) {
                cloudinaryService.getErrorMessage().removeObserver(uploadErrorObserver);
            }
            Log.d("AddEditPostDialogFragment", "Trigger uploadAllImagesToCloudinary, selectedImageUris size: " + selectedImageUris.size());
            uploadAllImagesToCloudinary();
        }
    }

    private void uploadAllImagesToCloudinary() {
        uploadedImageUrls.clear();
        cloudinaryService.clearUploadedUrls();
        cloudinaryService.uploadMultipleImages(selectedImageUris, "posts");
        uploadUrlsObserver = urls -> {
            if (urls != null && urls.size() == selectedImageUris.size()) {
                uploadedImageUrls.clear();
                uploadedImageUrls.addAll(urls);
                isUploadingImages = false;
                btnSave.setEnabled(true);
            }
        };
        cloudinaryService.getUploadedUrls().observe(getViewLifecycleOwner(), uploadUrlsObserver);
        uploadErrorObserver = errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                isUploadingImages = false;
                btnSave.setEnabled(false);
                Toast.makeText(getContext(), "Lỗi upload ảnh: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        cloudinaryService.getErrorMessage().observe(getViewLifecycleOwner(), uploadErrorObserver);
    }

    private void savePost() {
        if (isUploadingImages) {
            Toast.makeText(getContext(), "Please wait for images upload to finish!", Toast.LENGTH_SHORT).show();
            return;
        }
        String titleStr = etTitle.getText().toString().trim();
        String descriptionStr = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        Double priceVal = null;
        if (!priceStr.isEmpty()) {
            try {
                priceVal = Double.parseDouble(priceStr);
                if (priceVal < 0) {
                    etPrice.setError("Price must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price format");
                return;
            }
        }
        if (titleStr.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }
        if (descriptionStr.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }
        Post postToSave = new Post();
        postToSave.setTitle(titleStr);
        postToSave.setDescription(descriptionStr);
        postToSave.setPrice(priceVal);
        postToSave.setImages(new ArrayList<>(uploadedImageUrls));
        if (listener != null) {
            listener.onPostSaved(postToSave, isEditMode);
        }
        dismiss();
    }

    public List<Uri> getSelectedImageUris() {
        return selectedImageUris;
    }

    public List<String> getUploadedImageUrls() {
        return uploadedImageUrls;
    }
} 