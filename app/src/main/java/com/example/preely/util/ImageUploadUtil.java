package com.example.preely.util;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.preely.model.entities.Image;
import com.example.preely.repository.MainRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.example.preely.util.Constraints.CollectionName;

public class ImageUploadUtil {

    private Context context;
    private StoreService storeService;
    private MainRepository<Image> imageRepository;
    private FirebaseFirestore db;

    public ImageUploadUtil(Context context) {
        this.context = context;
        this.storeService = new StoreService(context);
        this.imageRepository = new MainRepository<>(Image.class, CollectionName.IMAGE);
        this.db = FirebaseFirestore.getInstance();
    }

    public interface ImageUploadCallback {
        void onSuccess(Image image);
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
            // Create Image entity
            Image image = new Image();
            image.setPost_id(postId);
            image.setLink(imageUrl);

            // Save to Firestore using add method
//            imageRepository.add(image, "image", new DbUtil.OnInsertCallback() {
//                @Override
//                public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
//                    if (callback != null) {
//                        callback.onSuccess(image);
//                    }
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    if (callback != null) {
//                        callback.onError("Failed to save image: " + e.getMessage());
//                    }
//                }
//            });
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
            public void onSuccess(Image image) {
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
//        imageRepository.delete(imageId, "image", new DbUtil.OnDeleteCallBack() {
//            @Override
//            public void onSuccess() {
//                if (callback != null) {
//                    callback.onSuccess(null);
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                if (callback != null) {
//                    callback.onError("Failed to delete image: " + e.getMessage());
//                }
//            }
//        });
    }

    public void getImagesForPost(String postId, ImageListCallback callback) {
        // Query images by post_id
        Query query = db.collection("image").whereEqualTo("post_id", postId);
        imageRepository.getAll(query).observeForever(images -> {
            if (callback != null) {
                callback.onSuccess(images);
            }
        });
    }

    public interface ImageListCallback {
        void onSuccess(List<Image> images);
        void onError(String error);
    }
} 