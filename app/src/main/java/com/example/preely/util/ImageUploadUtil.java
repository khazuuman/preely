package com.example.preely.util;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.preely.repository.MainRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.example.preely.util.Constraints.CollectionName;

public class ImageUploadUtil {

    private Context context;
    private StoreService storeService;
    private FirebaseFirestore db;

    public ImageUploadUtil(Context context) {
        this.context = context;
        this.storeService = new StoreService(context);
        this.db = FirebaseFirestore.getInstance();
    }

    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public void uploadImageForPost(Uri imageUri, String postId, String fileName, ImageUploadCallback callback) {
        if (imageUri == null) {
            if (callback != null) {
                callback.onError("No image selected");
            }
            return;
        }

        // Upload to cloud storage
        CompletableFuture<String> uploadFuture = storeService.uploadFileAsync(imageUri, "posts", fileName);
        
        uploadFuture.thenAccept(imageUrl -> {
            if (callback != null) {
                callback.onSuccess(imageUrl);
            }
        }).exceptionally(throwable -> {
            if (callback != null) {
                callback.onError("Upload failed: " + throwable.getMessage());
            }
            return null;
        });
    }

    public void uploadMultipleImagesForPost(Uri[] imageUris, String postId, ImageUploadCallback callback) {
        if (imageUris == null || imageUris.length == 0) {
            if (callback != null) {
                callback.onError("No images selected");
            }
            return;
        }

        // Upload images sequentially
        uploadImageSequentially(imageUris, postId, 0, callback);
    }

    private void uploadImageSequentially(Uri[] imageUris, String postId, int index, ImageUploadCallback callback) {
        if (index >= imageUris.length) {
            if (callback != null) {
                callback.onSuccess(null); // All images uploaded
            }
            return;
        }

        String fileName = "post_" + postId + "_" + System.currentTimeMillis() + "_" + index;
        uploadImageForPost(imageUris[index], postId, fileName, new ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                // Continue with next image
                uploadImageSequentially(imageUris, postId, index + 1, callback);
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError("Failed to upload image " + (index + 1) + ": " + error);
                }
            }
        });
    }

    public void deleteImage(String imageId, ImageUploadCallback callback) {
    }

    public void getImagesForPost(String postId, ImageListCallback callback) {
    }

    public interface ImageListCallback {
        void onSuccess(List<String> imageUrls);
        void onError(String error);
    }
} 