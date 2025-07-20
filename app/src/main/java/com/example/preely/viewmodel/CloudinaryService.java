package com.example.preely.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CloudinaryService handles image upload operations to Cloudinary cloud storage.
 * This service extends AndroidViewModel to handle configuration and lifecycle management.
 * It provides functionality for single and multiple image uploads with progress tracking.
 */
public class CloudinaryService extends AndroidViewModel {
    private static final String TAG = "CloudinaryService";
    private static final String CLOUDINARY_URL_PATTERN = "cloudinary://([^:]+):([^@]+)@(.+)";
    
    // LiveData objects to track upload states and results
    private final MutableLiveData<String> uploadStatus = new MutableLiveData<>();
    private final MutableLiveData<Integer> uploadProgress = new MutableLiveData<>();
    private final MutableLiveData<List<String>> uploadedUrls = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> uploadedImageUrl = new MutableLiveData<>();
    private boolean isInitialized = false;

    // Default Cloudinary configuration
    private static final String CLOUD_NAME = "dpsgcdrlx";
    private static final String API_KEY = "959226593637423";
    private static final String API_SECRET = "p54qEoP00iChUMofQX9SoeLuOsk";

    public CloudinaryService(@NonNull Application application) {
        super(application);
        // Auto initialize with default config
        initCloudinary(CLOUD_NAME, API_KEY, API_SECRET);
    }

    /**
     * Initialize Cloudinary from URL string
     * URL format: cloudinary://{api_key}:{api_secret}@{cloud_name}
     * @param cloudinaryUrl The complete Cloudinary URL containing credentials
     */
    public void initFromUrl(String cloudinaryUrl) {
        Pattern pattern = Pattern.compile(CLOUDINARY_URL_PATTERN);
        Matcher matcher = pattern.matcher(cloudinaryUrl);
        
        if (matcher.find()) {
            String apiKey = matcher.group(1);
            String apiSecret = matcher.group(2);
            String cloudName = matcher.group(3);
            
            initCloudinary(cloudName, apiKey, apiSecret);
        } else {
            errorMessage.setValue("Invalid Cloudinary URL format");
            Log.e(TAG, "Invalid Cloudinary URL format");
        }
    }

    /**
     * Initialize Cloudinary with separate credentials
     * @param cloudName The cloud name from Cloudinary dashboard
     * @param apiKey The API key from Cloudinary dashboard
     * @param apiSecret The API secret from Cloudinary dashboard
     */
    public void initCloudinary(String cloudName, String apiKey, String apiSecret) {
        if (!isInitialized) {
            try {
                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", cloudName);
                config.put("api_key", apiKey);
                config.put("api_secret", apiSecret);
                
                MediaManager.init(getApplication(), config);
                isInitialized = true;
                uploadStatus.setValue("Initialized");
                Log.d(TAG, "Cloudinary initialized successfully");
            } catch (Exception e) {
                errorMessage.setValue("Error initializing Cloudinary: " + e.getMessage());
                Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
            }
        }
    }

    /**
     * Upload a single image to Cloudinary
     * @param imageUri The URI of the image to upload
     * @param folder The destination folder in Cloudinary
     */
    public void uploadImage(Uri imageUri, String folder) {
        if (!isInitialized) {
            errorMessage.setValue("Cloudinary not initialized");
            return;
        }

        // Reset URL trước khi upload mới
        uploadedImageUrl.setValue(null);
        uploadStatus.setValue("Uploading...");
        uploadProgress.setValue(0);

        try {
            String requestId = MediaManager.get()
                    .upload(imageUri)
                    .option("folder", folder)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            uploadStatus.postValue("Upload started");
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            uploadProgress.postValue(progress);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            
                            // Trả về URL qua LiveData
                            uploadedImageUrl.postValue(imageUrl);
                            
                            List<String> currentUrls = uploadedUrls.getValue();
                            if (currentUrls != null) {
                                currentUrls.add(imageUrl);
                                uploadedUrls.postValue(currentUrls);
                            }
                            uploadStatus.postValue("Upload completed");
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            errorMessage.postValue(error.getDescription());
                            uploadStatus.postValue("Upload failed");
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            uploadStatus.postValue("Upload rescheduled");
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            errorMessage.setValue("Error uploading image: " + e.getMessage());
            uploadStatus.setValue("Upload failed");
        }
    }

    /**
     * Upload multiple images to Cloudinary
     * Tracks overall progress and manages concurrent uploads
     * @param imageUris List of image URIs to upload
     * @param folder The destination folder in Cloudinary
     */
    public void uploadMultipleImages(List<Uri> imageUris, String folder) {
        if (!isInitialized) {
            errorMessage.setValue("Cloudinary not initialized");
            return;
        }

        uploadStatus.setValue("Uploading multiple images...");
        uploadProgress.setValue(0);

        final int[] totalImages = {imageUris.size()};
        final int[] completedUploads = {0};

        for (Uri imageUri : imageUris) {
            try {
                MediaManager.get()
                        .upload(imageUri)
                        .option("folder", folder)
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                // Individual upload started
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                // Progress for individual upload
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String imageUrl = (String) resultData.get("secure_url");
                                List<String> currentUrls = uploadedUrls.getValue();
                                if (currentUrls != null) {
                                    currentUrls.add(imageUrl);
                                    uploadedUrls.postValue(currentUrls);
                                }

                                completedUploads[0]++;
                                int progress = (completedUploads[0] * 100) / totalImages[0];
                                uploadProgress.postValue(progress);

                                if (completedUploads[0] == totalImages[0]) {
                                    uploadStatus.postValue("All uploads completed");
                                }
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                errorMessage.postValue("Error uploading image: " + error.getDescription());
                                completedUploads[0]++;
                                
                                if (completedUploads[0] == totalImages[0]) {
                                    uploadStatus.postValue("Uploads completed with errors");
                                }
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                uploadStatus.postValue("Upload rescheduled");
                            }
                        })
                        .dispatch();

            } catch (Exception e) {
                errorMessage.setValue("Error uploading image: " + e.getMessage());
                completedUploads[0]++;
                
                if (completedUploads[0] == totalImages[0]) {
                    uploadStatus.setValue("Uploads completed with errors");
                }
            }
        }
    }

    // LiveData getters for observing states and results
    
    /**
     * Get the current upload status
     * @return LiveData containing the current status message
     */
    public LiveData<String> getUploadStatus() {
        return uploadStatus;
    }

    /**
     * Get the current upload progress
     * @return LiveData containing the progress percentage (0-100)
     */
    public LiveData<Integer> getUploadProgress() {
        return uploadProgress;
    }

    /**
     * Get the list of uploaded image URLs
     * @return LiveData containing list of secure URLs from Cloudinary
     */
    public LiveData<List<String>> getUploadedUrls() {
        return uploadedUrls;
    }

    /**
     * Get any error messages
     * @return LiveData containing error messages if any
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the uploaded image URL (for single upload)
     * @return LiveData containing the uploaded image URL
     */
    public LiveData<String> getUploadedImageUrl() {
        return uploadedImageUrl;
    }

    /**
     * Clear the list of uploaded URLs
     */
    public void clearUploadedUrls() {
        uploadedUrls.setValue(new ArrayList<>());
    }
} 