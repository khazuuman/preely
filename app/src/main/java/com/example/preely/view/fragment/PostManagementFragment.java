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

import java.util.ArrayList;
import java.util.List;

public class PostManagementFragment extends Fragment implements PostAdapter.OnPostClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<Post> postList = new ArrayList<>();
    private List<Post> originalPostList = new ArrayList<>();
    private MainRepository<Post> postRepository;
    private PostAdapter postAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration postListener;
    private FirebaseFirestore db;

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
        postRepository = new MainRepository<>(Post.class, CollectionName.POSTS);
        realtimeUtil = new FirestoreRealtimeUtil();
        db = FirebaseFirestore.getInstance();
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
        postListener = realtimeUtil.listenToPosts(new FirestoreRealtimeUtil.RealtimeListener<Post>() {
            @Override
            public void onDataAdded(Post post) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Check if post already exists to avoid duplicate notifications
                        boolean postExists = originalPostList.stream()
                            .anyMatch(existingPost -> existingPost.getId().equals(post.getId()));
                        
                        if (!postExists) {
                            originalPostList.add(post);
                            postList.add(post);
                            postAdapter.setPostList(postList);
                            Toast.makeText(getContext(), "New post added: " + post.getTitle(), Toast.LENGTH_SHORT).show();
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
        Query query = db.collection("post");
        postRepository.getAll(query).observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                originalPostList.clear();
                postList.clear();
                originalPostList.addAll(posts);
                postList.addAll(posts);
                postAdapter.setPostList(postList);
            }
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddPostDialog();
        });
    }

    private void showAddPostDialog() {
        AddEditPostDialog dialog = new AddEditPostDialog(getContext(), null, 
            new AddEditPostDialog.OnPostDialogListener() {
                @Override
                public void onPostSaved(Post post, boolean isEdit) {
                    if (isEdit) {
                        updatePost(post);
                    } else {
                        savePost(post);
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
        postRepository.add(post, "post", new CallBackUtil.OnInsertCallback() {
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

    private void updatePost(Post post) {
        postRepository.update(post, post.getId().getId(), new CallBackUtil.OnUpdateCallback() {
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
                postRepository.delete(post.getId().getId(), new CallBackUtil.OnDeleteCallBack() {
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
        // Show post details dialog
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(post.getTitle()).append("\n");
        details.append("Description: ").append(post.getDescription()).append("\n");
        if (post.getPrice() != null) {
            details.append("Price: $").append(post.getPrice()).append("\n");
        }
        if (post.getCreate_at() != null) {
            details.append("Created: ").append(post.getCreate_at().toString());
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Post Details")
            .setMessage(details.toString())
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
        if (postListener != null) {
            postListener.remove();
        }
        realtimeUtil.removeAllListeners();
    }
} 