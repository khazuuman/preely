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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.PostAdapter;
import com.example.preely.dialog.AddEditPostDialog;
import com.example.preely.model.entities.Post;
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
import com.example.preely.util.ImageUploadUtil;
import android.net.Uri;
import java.util.List;

import java.util.ArrayList;
import java.util.List;
import com.example.preely.model.entities.Image;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Gravity;
import com.bumptech.glide.Glide;
import com.example.preely.viewmodel.ManagementPostService;

public class PostManagementFragment extends Fragment implements PostAdapter.OnPostClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<Post> postList = new ArrayList<>();
    private List<Post> originalPostList = new ArrayList<>();
    private List<Post> allPostList = new ArrayList<>();
    private List<Post> pagedPostList = new ArrayList<>();
    private ManagementPostService postService;
    private PostAdapter postAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration postListener;
    private FirebaseFirestore db;
    private boolean isInitialLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadPosts(); // Load data first
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
        recyclerView = view.findViewById(R.id.recycler_posts);
        fabAdd = view.findViewById(R.id.fab_add_post);
        etSearch = view.findViewById(R.id.et_search_posts);
        postService = new ManagementPostService();
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter();
        postAdapter.setOnPostClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(postAdapter);
    }

    private void setupSearch() {
        SearchFilterUtil.setupPostSearch(etSearch, originalPostList, postAdapter, 
            new SearchFilterUtil.SearchFilterCallback<Post>() {
                @Override
                public void onFiltered(List<Post> filteredList) {
                    postList.clear();
                    postList.addAll(filteredList);
                }
            });
    }

    private void setupRealtimeListener() {
        postService.listenRealtime(new FirestoreRealtimeUtil.RealtimeListener<Post>() {
            @Override
            public void onDataAdded(Post post) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        boolean postExists = originalPostList.stream()
                            .anyMatch(existingPost -> existingPost.getId().equals(post.getId()));
                        if (!postExists) {
                            originalPostList.add(post);
                            postList.add(post);
                            postAdapter.setPostList(postList);
                            if (!isInitialLoad) {
                                Toast.makeText(getContext(), "New post added: " + post.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            @Override
            public void onDataModified(Post post) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updatePostInList(originalPostList, post);
                        updatePostInList(postList, post);
                        postAdapter.setPostList(postList);
                        Toast.makeText(getContext(), "Post updated: " + post.getTitle(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
            @Override
            public void onDataRemoved(Post post) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        removePostFromList(originalPostList, post);
                        removePostFromList(postList, post);
                        postAdapter.setPostList(postList);
                        Toast.makeText(getContext(), "Post removed: " + post.getTitle(), Toast.LENGTH_SHORT).show();
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
        recyclerView.postDelayed(() -> isInitialLoad = false, 1000);
    }

    private void updatePostInList(List<Post> list, Post updatedPost) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedPost.getId())) {
                list.set(i, updatedPost);
                break;
            }
        }
    }

    private void removePostFromList(List<Post> list, Post postToRemove) {
        list.removeIf(post -> post.getId().equals(postToRemove.getId()));
    }

    private void loadPosts() {
        postService.getAllPosts(posts -> {
            if (posts != null) {
                allPostList.clear();
                pagedPostList.clear();
                allPostList.addAll(posts);
                pagedPostList.addAll(PaginationUtil.getPageItems(allPostList, 0));
                postAdapter.setPostList(pagedPostList);
                PaginationUtil.resetPagination();
                setupPagination();
            }
        });
    }

    private void setupPagination() {
        PaginationUtil.setupPagination(recyclerView, allPostList, new PaginationUtil.PaginationCallback<Post>() {
            @Override
            public void onLoadMore(List<Post> newItems, int page) {
                int oldSize = pagedPostList.size();
                pagedPostList.addAll(newItems);
                recyclerView.post(() -> postAdapter.notifyItemRangeInserted(oldSize, newItems.size()));
            }
            @Override
            public void onLoadComplete() {}
            @Override
            public void onError(String error) {}
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddPostDialog();
        });
    }

    private void showAddPostDialog() {
        final AddEditPostDialog dialog = new AddEditPostDialog(getContext(), null, null);
        dialog.setOnPostDialogListener(new AddEditPostDialog.OnPostDialogListener() {
            @Override
            public void onPostSaved(Post post, boolean isEdit) {
                if (isEdit) {
                    updatePost(post);
                } else {
                    List<Uri> imageUris = dialog.getSelectedImageUris();
                    savePostAndUploadImages(post, imageUris);
                }
            }
        });
        dialog.show();
    }

    private void showEditPostDialog(Post post) {
        AddEditPostDialog dialog = new AddEditPostDialog(getContext(), post, 
            new AddEditPostDialog.OnPostDialogListener() {
                @Override
                public void onPostSaved(Post post, boolean isEdit) {
                    updatePost(post);
                }
            });
        dialog.show();
    }

    private void savePost(Post post) {
        postService.addPost(post, new CallBackUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "Post saved successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error saving post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePostAndUploadImages(Post post, List<Uri> imageUris) {
        postService.addPostAndUploadImages(post, imageUris, getContext(), new com.example.preely.util.ImageUploadUtil.ImageUploadCallback() {
            @Override
            public void onSuccess(com.example.preely.model.entities.Image image) {
                // Có thể cập nhật UI hoặc log nếu muốn
            }
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Upload image failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePost(Post post) {
        postService.updatePost(post, new CallBackUtil.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Post updated successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error updating post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePost(Post post) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete \"" + post.getTitle() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                postService.deletePost(post, new CallBackUtil.OnDeleteCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onPostClick(Post post) {
        // Custom dialog layout đẹp
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 32);
        layout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        // Tiêu đề
        android.widget.TextView titleTv = new android.widget.TextView(getContext());
        titleTv.setText(post.getTitle() != null ? post.getTitle() : "N/A");
        titleTv.setTextSize(22);
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        titleTv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layout.addView(titleTv);

        // Mô tả
        android.widget.TextView descTv = new android.widget.TextView(getContext());
        descTv.setText(post.getDescription() != null ? post.getDescription() : "");
        descTv.setTextSize(16);
        descTv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        descTv.setPadding(0, 8, 0, 8);
        layout.addView(descTv);

        // Giá và ngày tạo
        android.widget.TextView infoTv = new android.widget.TextView(getContext());
        String info = "";
        if (post.getPrice() != null) info += "Price: $" + post.getPrice() + "  ";
        if (post.getCreate_at() != null) info += "Created: " + post.getCreate_at().toString();
        infoTv.setText(info);
        infoTv.setTextSize(15);
        infoTv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layout.addView(infoTv);

        // Preview ảnh grid
        android.widget.LinearLayout grid = new android.widget.LinearLayout(getContext());
        grid.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        grid.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        grid.setPadding(0, 16, 0, 0);
        layout.addView(grid);

        // Lấy ảnh từ Firestore
        ImageUploadUtil imageUploadUtil = new ImageUploadUtil(getContext());
        imageUploadUtil.getImagesForPost(post.getId().getId(), new ImageUploadUtil.ImageListCallback() {
            @Override
            public void onSuccess(List<com.example.preely.model.entities.Image> images) {
                grid.removeAllViews();
                for (com.example.preely.model.entities.Image img : images) {
                    android.widget.ImageView imageView = new android.widget.ImageView(getContext());
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(180, 180);
                    params.setMargins(8, 8, 8, 8);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    imageView.setBackgroundResource(R.drawable.rounded_edge);
                    Glide.with(getContext()).load(img.getLink()).placeholder(R.drawable.ic_account).into(imageView);
                    grid.addView(imageView);
                }
            }
            @Override
            public void onError(String error) {}
        });

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("Post Details")
            .setView(layout)
            .setPositiveButton("Edit", (dialog, which) -> showEditPostDialog(post))
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onPostEdit(Post post) {
        showEditPostDialog(post);
    }

    @Override
    public void onPostDelete(Post post) {
        deletePost(post);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        postService.removeRealtimeListener();
    }
} 