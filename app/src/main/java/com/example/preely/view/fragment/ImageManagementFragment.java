package com.example.preely.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.ImageAdapter;
import com.example.preely.dialog.AddEditImageDialog;
import com.example.preely.model.entities.Image;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.example.preely.util.PaginationUtil;
import com.example.preely.util.SearchFilterUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import com.example.preely.util.DbUtil;
import com.example.preely.util.Constraints.*;

import java.util.ArrayList;
import java.util.List;

public class ImageManagementFragment extends Fragment implements ImageAdapter.OnImageClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<Image> imageList = new ArrayList<>();
    private List<Image> originalImageList = new ArrayList<>();
    private MainRepository<Image> imageRepository;
    private ImageAdapter imageAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration imageListener;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadImages(); // Load data first
        setupListeners();
        
        // Setup real-time listener after a short delay to ensure data is loaded
        view.post(() -> {
            if (isAdded()) { // Check if fragment is still attached
                setupRealtimeListener();
            }
        });
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_images);
        fabAdd = view.findViewById(R.id.fab_add_image);
        etSearch = view.findViewById(R.id.et_search_images);
        imageRepository = new MainRepository<>(Image.class, CollectionName.IMAGE);
        realtimeUtil = new FirestoreRealtimeUtil();
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        imageAdapter = new ImageAdapter();
        imageAdapter.setOnImageClickListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(imageAdapter);
    }

    private void setupSearch() {
        SearchFilterUtil.setupImageSearch(etSearch, originalImageList, imageAdapter, 
            new SearchFilterUtil.SearchFilterCallback<Image>() {
                @Override
                public void onFiltered(List<Image> filteredList) {
                    imageList.clear();
                    imageList.addAll(filteredList);
                }
            });
    }

    private void setupRealtimeListener() {
        imageListener = realtimeUtil.listenToImages(new FirestoreRealtimeUtil.RealtimeListener<Image>() {
            @Override
            public void onDataAdded(Image image) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Check if image already exists to avoid duplicate notifications
                        boolean imageExists = originalImageList.stream()
                            .anyMatch(existingImage -> existingImage.getId().equals(image.getId()));
                        
                        if (!imageExists) {
                            originalImageList.add(image);
                            imageList.add(image);
                            imageAdapter.setImageList(imageList);
                            Toast.makeText(getContext(), "New image added: " + image.getId(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onDataModified(Image image) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateImageInList(originalImageList, image);
                        updateImageInList(imageList, image);
                        imageAdapter.setImageList(imageList);
                        Toast.makeText(getContext(), "Image updated: " + image.getId(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onDataRemoved(Image image) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        removeImageFromList(originalImageList, image);
                        removeImageFromList(imageList, image);
                        imageAdapter.setImageList(imageList);
                        Toast.makeText(getContext(), "Image removed: " + image.getId(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Real-time error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateImageInList(List<Image> list, Image updatedImage) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedImage.getId())) {
                list.set(i, updatedImage);
                break;
            }
        }
    }

    private void removeImageFromList(List<Image> list, Image imageToRemove) {
        list.removeIf(image -> image.getId().equals(imageToRemove.getId()));
    }

    private void loadImages() {
        Query query = db.collection("image");
        imageRepository.getAll(query).observe(getViewLifecycleOwner(), images -> {
            if (images != null) {
                originalImageList.clear();
                imageList.clear();
                originalImageList.addAll(images);
                imageList.addAll(images);
                imageAdapter.setImageList(imageList);
            }
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddImageDialog();
        });
    }

    private void showAddImageDialog() {
        AddEditImageDialog dialog = new AddEditImageDialog(getContext(), null, 
            new AddEditImageDialog.OnImageDialogListener() {
                @Override
                public void onImageSaved(Image image, boolean isEdit) {
                    if (isEdit) {
                        updateImage(image);
                    } else {
                        saveImage(image);
                    }
                }
            });
        dialog.show();
    }

    private void showEditImageDialog(Image image) {
        AddEditImageDialog dialog = new AddEditImageDialog(getContext(), image, 
            new AddEditImageDialog.OnImageDialogListener() {
                @Override
                public void onImageSaved(Image image, boolean isEdit) {
                    updateImage(image);
                }
            });
        dialog.show();
    }

    private void saveImage(Image image) {
        imageRepository.add(image, "image", new CallBackUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImage(Image image) {
        imageRepository.update(image, image.getId().getId(), new CallBackUtil.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Image updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error updating image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onImageClick(Image image) {
        // Show image details dialog
        new AlertDialog.Builder(getContext())
            .setTitle("Image Details")
            .setMessage("Post ID: " + image.getPost_id() + "\n" +
                       "Link: " + image.getLink())
            .setPositiveButton("Edit", (dialog, which) -> showEditImageDialog(image))
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onImageEdit(Image image) {
        showEditImageDialog(image);
    }

    @Override
    public void onImageDelete(Image image) {
        deleteImage(image);
    }

    private void deleteImage(Image image) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete", (dialog, which) -> {
                imageRepository.delete(image.getId().getId(), new CallBackUtil.OnDeleteCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (imageListener != null) {
            imageListener.remove();
        }
        realtimeUtil.removeAllListeners();
    }
} 