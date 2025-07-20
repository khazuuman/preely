package com.example.preely.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickerUtil {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;
    public static final int PERMISSION_REQUEST_CODE = 100;

    private Fragment fragment;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private OnImageSelectedListener listener;
    private String currentPhotoPath;

    public interface OnImageSelectedListener {
        void onImageSelected(Uri imageUri, String imagePath);
        void onError(String error);
    }

    public ImagePickerUtil(Fragment fragment, OnImageSelectedListener listener) {
        this.fragment = fragment;
        this.listener = listener;
        setupLaunchers();
    }

    private void setupLaunchers() {
        cameraLauncher = fragment.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (currentPhotoPath != null) {
                        File photoFile = new File(currentPhotoPath);
                        Uri photoUri = FileProvider.getUriForFile(
                            fragment.requireContext(),
                            fragment.requireContext().getPackageName() + ".fileprovider",
                            photoFile
                        );
                        if (listener != null) {
                            listener.onImageSelected(photoUri, currentPhotoPath);
                        }
                    }
                }
            }
        );

        galleryLauncher = fragment.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null && listener != null) {
                        listener.onImageSelected(selectedImage, selectedImage.toString());
                    }
                }
            }
        );

        permissionLauncher = fragment.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    showImageSourceDialog();
                } else {
                    if (listener != null) {
                        listener.onError("Camera permission is required");
                    }
                }
            }
        );
    }

    public void showImagePicker() {
        if (checkPermissions()) {
            showImageSourceDialog();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(fragment.requireContext(), 
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(fragment.requireContext(), 
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(fragment.requireContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                if (listener != null) {
                    listener.onError("Error creating image file");
                }
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                    fragment.requireContext(),
                    fragment.requireContext().getPackageName() + ".fileprovider",
                    photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = fragment.requireContext().getExternalFilesDir("Images");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
            PERMISSION_REQUEST_CODE);
    }
} 