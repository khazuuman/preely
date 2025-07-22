package com.example.preely.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.preely.model.entities.Post;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.preely.repository.MainRepository;
import com.example.preely.model.entities.SavedPost;
import com.example.preely.util.Constraints.*;

/**
 * SavedPostService manages the bookmarking functionality for posts.
 * This service handles saving, unsaving, and retrieving saved posts using Firebase Firestore.
 * It extends AndroidViewModel to handle lifecycle and provide LiveData updates.
 */
public class SavedPostService extends AndroidViewModel {
    private static final String TAG = "SavedPostService";
    private static final int POSTS_PER_PAGE = 10;
    private final FirebaseFirestore db;
    // LiveData objects to track saved posts and operation states
    private final MutableLiveData<List<Post>> SavedPosts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Post>> allSavedPosts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> status = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private DocumentSnapshot lastVisible = null;
    private boolean isLastPage = false;

    private MainRepository<SavedPost> SavedPostRepository;
    private MainRepository<Post> postRepository;

    public SavedPostService(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        SavedPostRepository = new MainRepository<>(SavedPost.class, CollectionName.SAVED_POST);
        postRepository = new MainRepository<>(Post.class, CollectionName.POSTS);
    }

    /**
     * Save a post for a user
     * Checks if the post is already saved before creating a new saved post record
     * Updates LiveData with operation status
     *
     * @param userId User ID who is saving the post
     * @param postId Post ID to be saved
     */
    public void savePost(String userId, String postId) {
        if (userId == null || postId == null) {
            error.setValue("User ID and Post ID cannot be null");
            return;
        }

        DocumentReference userRef = db.collection("user").document(userId);
        DocumentReference postRef = db.collection("post").document(postId);

        // Check if post is already saved
        db.collection("SavedPosts")
                .whereEqualTo("user_id", userRef)
                .whereEqualTo("post_id", postRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // Create new saved post record with timestamp
                            Map<String, Object> SavedPost = new HashMap<>();
                            SavedPost.put("user_id", userRef);
                            SavedPost.put("post_id", postRef);
                            SavedPost.put("save_date", new Date());

                            db.collection("SavedPosts")
                                    .add(SavedPost)
                                    .addOnSuccessListener(documentReference -> {
                                        status.setValue("Post saved successfully");
                                        // Refresh saved posts list
                                        getSavedPosts(userId);
                                    })
                                    .addOnFailureListener(e -> {
                                        error.setValue("Failed to save post: " + e.getMessage());
                                        Log.e(TAG, "Error saving post", e);
                                    });
                        } else {
                            error.setValue("Post is already saved");
                        }
                    } else {
                        error.setValue("Failed to check saved status: " + Objects.requireNonNull(task.getException()).getMessage());
                        Log.e(TAG, "Error checking saved status", task.getException());
                    }
                });
    }

    /**
     * Unsave (remove) a saved post for a user
     * Finds and deletes the saved post record
     * Updates LiveData with operation status
     *
     * @param userId User ID who is unsaving the post
     * @param postId Post ID to be unsaved
     */
    public void unsavePost(String userId, String postId) {
        if (userId == null || postId == null) {
            error.setValue("User ID and Post ID cannot be null");
            return;
        }
        DocumentReference userRef = db.collection("user").document(userId);
        DocumentReference postRef = db.collection("post").document(postId);
        db.collection("SavedPosts")
                .whereEqualTo("user_id", userRef)
                .whereEqualTo("post_id", postRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean found = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            found = true;
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        status.setValue("Post unsaved successfully");
                                        // Refresh saved posts list
                                        getSavedPosts(userId);
                                    })
                                    .addOnFailureListener(e -> {
                                        error.setValue("Failed to unsave post: " + e.getMessage());
                                        Log.e(TAG, "Error unsaving post", e);
                                    });
                        }
                        if (!found) {
                            error.setValue("Post was not saved");
                        }
                    } else {
                        error.setValue("Failed to check saved status: " + Objects.requireNonNull(task.getException()).getMessage());
                        Log.e(TAG, "Error checking saved status", task.getException());
                    }
                });
    }

    /**
     * Retrieve all saved posts for a user
     * Fetches saved post records and then loads full post details
     * Results are ordered by save date (newest first)
     *
     * @param userId User ID whose saved posts to fetch
     */
    public void getSavedPosts(String userId) {
        if (userId == null) {
            error.setValue("User ID cannot be null");
            Log.e("DEBUG", "[SavedPostService] userId is null");
            return;
        }
        DocumentReference userRef = db.collection("user").document(userId);
        Log.d("DEBUG", "[SavedPostService] Querying SavedPosts for userRef: " + userRef.getPath());
        db.collection("SavedPosts")
                .whereEqualTo("user_id", userRef)
                .orderBy("save_date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> postIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Object postIdObj = document.get("post_id");
                            String postId = null;
                            if (postIdObj instanceof DocumentReference) {
                                postId = ((DocumentReference) postIdObj).getId();
                            } else if (postIdObj instanceof String) {
                                String raw = (String) postIdObj;
                                postId = raw.contains("/") ? raw.substring(raw.lastIndexOf("/") + 1) : raw;
                            }
                            Log.d("DEBUG", "[SavedPostService] Fetched postId: " + postId);
                            if (postId != null) {
                                postIds.add(postId);
                            }
                        }
                        Log.d("DEBUG", "[SavedPostService] Total postIds: " + postIds.size());
                        if (!postIds.isEmpty()) {
                            fetchPostDetails(postIds);
                        } else {
                            Log.d("DEBUG", "[SavedPostService] No postIds found");
                            SavedPosts.setValue(new ArrayList<>());
                        }
                    } else {
                        error.setValue("Failed to get saved posts: " + Objects.requireNonNull(task.getException()).getMessage());
                        Log.e("DEBUG", "[SavedPostService] Error getting saved posts", task.getException());
                    }
                });
    }

    /**
     * Check if a specific post is saved by a user
     * Updates status LiveData with the result
     *
     * @param userId User ID to check
     * @param postId Post ID to check
     */
    public void checkIfPostSaved(String userId, String postId) {
        if (userId == null || postId == null) {
            error.setValue("User ID and Post ID cannot be null");
            return;
        }
        DocumentReference userRef = db.collection("user").document(userId);
        DocumentReference postRef = db.collection("post").document(postId);
        db.collection("SavedPosts")
                .whereEqualTo("user_id", userRef)
                .whereEqualTo("post_id", postRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isSaved = !task.getResult().isEmpty();
                        status.setValue(isSaved ? "Post is saved" : "Post is not saved");
                    } else {
                        error.setValue("Failed to check saved status: " + Objects.requireNonNull(task.getException()).getMessage());
                        Log.e(TAG, "Error checking saved status", task.getException());
                    }
                });
    }

    /**
     * Helper method to fetch full post details for a list of post IDs
     * Updates SavedPosts LiveData when all posts are fetched
     *
     * @param postIds List of post IDs to fetch details for
     */
    private void fetchPostDetails(List<String> postIds) {
        Log.d("DEBUG", "[SavedPostService] fetchPostDetails called with postIds: " + postIds);
        List<Post> posts = new ArrayList<>();
        final int[] completedQueries = {0};

        for (String postId : postIds) {
            Log.d("DEBUG", "[SavedPostService] Fetching postId: " + postId);
            db.collection("post")
                    .document(postId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Post post = documentSnapshot.toObject(Post.class);
                            if (post != null) {
                                post.setId(documentSnapshot.getReference());
                                posts.add(post);
                                Log.d("DEBUG", "[SavedPostService] Fetched post: " + post.getId());
                            } else {
                                Log.d("DEBUG", "[SavedPostService] Post is null for id: " + postId);
                            }
                        } else {
                            Log.d("DEBUG", "[SavedPostService] Document does not exist for id: " + postId);
                        }
                        completedQueries[0]++;
                        if (completedQueries[0] == postIds.size()) {
                            Log.d("DEBUG", "[SavedPostService] All posts fetched. Total: " + posts.size());
                            SavedPosts.setValue(posts);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DEBUG", "[SavedPostService] Failed to fetch post details: " + e.getMessage());
                        completedQueries[0]++;
                        if (completedQueries[0] == postIds.size()) {
                            Log.d("DEBUG", "[SavedPostService] All posts fetched (with errors). Total: " + posts.size());
                            SavedPosts.setValue(posts);
                        }
                    });
        }
    }

    /**
     * Get saved posts with pagination
     * Loads POSTS_PER_PAGE posts at a time
     *
     * @param userId  User ID whose saved posts to fetch
     * @param refresh If true, resets pagination and loads first page
     */
    public void getListSavedPosts(String userId, boolean refresh) {
        if (userId == null) {
            error.setValue("User ID cannot be null");
            Log.e("DEBUG", "[SavedPostService] userId is null (getListSavedPosts)");
            return;
        }

        if (isLoading.getValue() != null && isLoading.getValue()) {
            Log.d("DEBUG", "[SavedPostService] Already loading, skip");
            return; // Prevent multiple simultaneous loads
        }

        if (refresh) {
            lastVisible = null;
            isLastPage = false;
            allSavedPosts.setValue(new ArrayList<>());
        }

        if (isLastPage) {
            Log.d("DEBUG", "[SavedPostService] isLastPage=true, skip");
            return; // No more posts to load
        }

        isLoading.setValue(true);

        DocumentReference userRef = db.collection("user").document(userId);
        Log.d("DEBUG", "[SavedPostService] Querying SavedPosts for userRef: " + userRef.getPath() + " (pagination)");
        Query query = db.collection("SavedPosts")
                .whereEqualTo("user_id", userRef)
                .orderBy("save_date", Query.Direction.DESCENDING)
                .limit(POSTS_PER_PAGE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> postIds = new ArrayList<>();
                List<QueryDocumentSnapshot> documents = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    documents.add(doc);
                }

                if (!documents.isEmpty()) {
                    lastVisible = documents.get(documents.size() - 1);

                    for (QueryDocumentSnapshot document : documents) {
                        Object postIdObj = document.get("post_id");
                        String postId = null;
                        if (postIdObj instanceof DocumentReference) {
                            postId = ((DocumentReference) postIdObj).getId();
                        } else if (postIdObj instanceof String) {
                            String raw = (String) postIdObj;
                            postId = raw.contains("/") ? raw.substring(raw.lastIndexOf("/") + 1) : raw;
                        }
                        Log.d("DEBUG", "[SavedPostService] (pagination) Fetched postId: " + postId);
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }
                    Log.d("DEBUG", "[SavedPostService] (pagination) Total postIds: " + postIds.size());
                    if (documents.size() < POSTS_PER_PAGE) {
                        isLastPage = true;
                    }
                } else {
                    isLastPage = true;
                }

                if (!postIds.isEmpty()) {
                    fetchPostDetailsForList(postIds);
                } else {
                    Log.d("DEBUG", "[SavedPostService] (pagination) No postIds found");
                    isLoading.setValue(false);
                }
            } else {
                error.setValue("Failed to get saved posts: " + Objects.requireNonNull(task.getException()).getMessage());
                Log.e("DEBUG", "[SavedPostService] (pagination) Error getting saved posts", task.getException());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Helper method to fetch post details for pagination
     * Updates allSavedPosts LiveData when posts are fetched
     *
     * @param postIds List of post IDs to fetch details for
     */
    private void fetchPostDetailsForList(List<String> postIds) {
        Log.d("DEBUG", "[SavedPostService] fetchPostDetailsForList called with postIds: " + postIds);
        List<Post> currentPosts = allSavedPosts.getValue() != null ?
                new ArrayList<>(allSavedPosts.getValue()) :
                new ArrayList<>();
        final int[] completedQueries = {0};

        for (String postId : postIds) {
            Log.d("DEBUG", "[SavedPostService] (pagination) Fetching postId: " + postId);
            db.collection("post")
                    .document(postId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Post post = documentSnapshot.toObject(Post.class);
                            if (post != null) {
                                post.setId(documentSnapshot.getReference());
                                currentPosts.add(post);
                                Log.d("DEBUG", "[SavedPostService] (pagination) Fetched post: " + post.getId());
                            } else {
                                Log.d("DEBUG", "[SavedPostService] (pagination) Post is null for id: " + postId);
                            }
                        } else {
                            Log.d("DEBUG", "[SavedPostService] (pagination) Document does not exist for id: " + postId);
                        }
                        completedQueries[0]++;
                        if (completedQueries[0] == postIds.size()) {
                            Log.d("DEBUG", "[SavedPostService] (pagination) All posts fetched. Total: " + currentPosts.size());
                            allSavedPosts.setValue(currentPosts);
                            isLoading.setValue(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DEBUG", "[SavedPostService] (pagination) Failed to fetch post details: " + e.getMessage());
                        completedQueries[0]++;
                        if (completedQueries[0] == postIds.size()) {
                            Log.d("DEBUG", "[SavedPostService] (pagination) All posts fetched (with errors). Total: " + currentPosts.size());
                            allSavedPosts.setValue(currentPosts);
                            isLoading.setValue(false);
                        }
                    });
        }
    }

    /**
     * Lấy danh sách SavedPost theo userId bằng MainRepository
     */
    public LiveData<List<SavedPost>> getSavedPostsByUser(String userId) {
        DocumentReference userRef = db.collection("user").document(userId);
        Query query = db.collection("SavedPosts").whereEqualTo("user_id", userRef);
        return SavedPostRepository.getAll(query);
    }

    /**
     * Lấy chi tiết các Post mà user đã lưu bằng MainRepository
     */
    public LiveData<List<Post>> getSavedPostsDetailByUser(String userId) {
        MutableLiveData<List<Post>> result = new MutableLiveData<>();
        getSavedPostsByUser(userId).observeForever(SavedPosts -> {
            if (SavedPosts == null || SavedPosts.isEmpty()) {
                result.setValue(new ArrayList<>());
                return;
            }
            List<Post> posts = new ArrayList<>();
            final int[] completed = {0};
            for (SavedPost sp : SavedPosts) {
                Query postQuery = db.collection("posts").whereEqualTo("id", sp.getPost_id());
                postRepository.getAll(postQuery).observeForever(postList -> {
                    if (postList != null && !postList.isEmpty()) {
                        posts.add(postList.get(0));
                    }
                    completed[0]++;
                    if (completed[0] == SavedPosts.size()) {
                        result.setValue(posts);
                    }
                });
            }
        });
        return result;
    }

    /**
     * Check if there are more posts to load
     *
     * @return true if all posts have been loaded
     */
    public boolean isLastPage() {
        return isLastPage;
    }

    /**
     * Get loading state
     *
     * @return LiveData containing loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Get all saved posts with pagination
     *
     * @return LiveData containing list of all loaded saved posts
     */
    public LiveData<List<Post>> getAllSavedPosts() {
        return allSavedPosts;
    }

    // LiveData getters for observing states and results

    /**
     * Get the list of saved posts
     *
     * @return LiveData containing list of saved Post objects
     */
    public LiveData<List<Post>> getSavedPostsLiveData() {
        return SavedPosts;
    }

    /**
     * Get the current operation status
     *
     * @return LiveData containing status messages
     */
    public LiveData<String> getStatus() {
        return status;
    }

    /**
     * Get any error messages
     *
     * @return LiveData containing error messages if any
     */
    public LiveData<String> getError() {
        return error;
    }
} 