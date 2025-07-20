package com.example.preely.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Image;
import com.example.preely.model.entities.Post;
import com.example.preely.model.entities.Tag;
import com.example.preely.model.entities.Transaction;
import com.example.preely.model.entities.User;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreRealtimeUtil {

    private static final String TAG = "FirestoreRealtimeUtil";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<ListenerRegistration> listeners = new ArrayList<>();

    public interface RealtimeListener<T> {
        void onDataAdded(T item);
        void onDataModified(T item);
        void onDataRemoved(T item);
        void onError(String error);
    }

    // User real-time listener
    public ListenerRegistration listenToUsers(RealtimeListener<User> listener) {
        ListenerRegistration registration = db.collection("user")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to users", error);
                    if (listener != null) {
                        listener.onError(error.getMessage());
                    }
                    return;
                }

                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        User user = dc.getDocument().toObject(User.class);
                        if (user != null) {
                            user.setId(dc.getDocument().getReference());

                            switch (dc.getType()) {
                                case ADDED:
                                    if (listener != null) listener.onDataAdded(user);
                                    break;
                                case MODIFIED:
                                    if (listener != null) listener.onDataModified(user);
                                    break;
                                case REMOVED:
                                    if (listener != null) listener.onDataRemoved(user);
                                    break;
                            }
                        }
                    }
                }
            });
        
        listeners.add(registration);
        return registration;
    }

    // Post real-time listener
    public ListenerRegistration listenToPosts(RealtimeListener<Post> listener) {
        ListenerRegistration registration = db.collection("post")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to posts", error);
                    if (listener != null) {
                        listener.onError(error.getMessage());
                    }
                    return;
                }

                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        Post post = dc.getDocument().toObject(Post.class);
                        if (post != null) {
                            post.setId(dc.getDocument().getReference());
                            
                            switch (dc.getType()) {
                                case ADDED:
                                    if (listener != null) listener.onDataAdded(post);
                                    break;
                                case MODIFIED:
                                    if (listener != null) listener.onDataModified(post);
                                    break;
                                case REMOVED:
                                    if (listener != null) listener.onDataRemoved(post);
                                    break;
                            }
                        }
                    }
                }
            });
        
        listeners.add(registration);
        return registration;
    }

    // Transaction real-time listener
    public ListenerRegistration listenToTransactions(RealtimeListener<Transaction> listener) {
        ListenerRegistration registration = db.collection("transaction")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to transactions", error);
                    if (listener != null) {
                        listener.onError(error.getMessage());
                    }
                    return;
                }

                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        Transaction transaction = dc.getDocument().toObject(Transaction.class);
                        if (transaction != null) {
                            transaction.setId(dc.getDocument().getReference());
                            
                            switch (dc.getType()) {
                                case ADDED:
                                    if (listener != null) listener.onDataAdded(transaction);
                                    break;
                                case MODIFIED:
                                    if (listener != null) listener.onDataModified(transaction);
                                    break;
                                case REMOVED:
                                    if (listener != null) listener.onDataRemoved(transaction);
                                    break;
                            }
                        }
                    }
                }
            });
        
        listeners.add(registration);
        return registration;
    }

    // Category real-time listener
    public ListenerRegistration listenToCategories(RealtimeListener<Category> listener) {
        ListenerRegistration registration = db.collection("category")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to categories", error);
                    if (listener != null) {
                        listener.onError(error.getMessage());
                    }
                    return;
                }

                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        Category category = dc.getDocument().toObject(Category.class);
                        if (category != null) {
                            category.setId(dc.getDocument().getReference());
                            
                            switch (dc.getType()) {
                                case ADDED:
                                    if (listener != null) listener.onDataAdded(category);
                                    break;
                                case MODIFIED:
                                    if (listener != null) listener.onDataModified(category);
                                    break;
                                case REMOVED:
                                    if (listener != null) listener.onDataRemoved(category);
                                    break;
                            }
                        }
                    }
                }
            });
        
        listeners.add(registration);
        return registration;
    }

    // Tag real-time listener
    public ListenerRegistration listenToTags(RealtimeListener<Tag> listener) {
        ListenerRegistration registration = db.collection("tag")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to tags", error);
                    if (listener != null) {
                        listener.onError(error.getMessage());
                    }
                    return;
                }

                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        Tag tag = dc.getDocument().toObject(Tag.class);
                        if (tag != null) {
                            tag.setId(dc.getDocument().getReference());
                            
                            switch (dc.getType()) {
                                case ADDED:
                                    if (listener != null) listener.onDataAdded(tag);
                                    break;
                                case MODIFIED:
                                    if (listener != null) listener.onDataModified(tag);
                                    break;
                                case REMOVED:
                                    if (listener != null) listener.onDataRemoved(tag);
                                    break;
                            }
                        }
                    }
                }
            });
        
        listeners.add(registration);
        return registration;
    }

    // Image real-time listener
    public ListenerRegistration listenToImages(RealtimeListener<Image> listener) {
        ListenerRegistration registration = db.collection("image")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to images", error);
                    if (listener != null) {
                        listener.onError(error.getMessage());
                    }
                    return;
                }

                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        Image image = dc.getDocument().toObject(Image.class);
                        if (image != null) {
                            image.setId(dc.getDocument().getReference());
                            
                            switch (dc.getType()) {
                                case ADDED:
                                    if (listener != null) listener.onDataAdded(image);
                                    break;
                                case MODIFIED:
                                    if (listener != null) listener.onDataModified(image);
                                    break;
                                case REMOVED:
                                    if (listener != null) listener.onDataRemoved(image);
                                    break;
                            }
                        }
                    }
                }
            });
        
        listeners.add(registration);
        return registration;
    }

    // Custom query listener
    public <T> ListenerRegistration listenToCustomQuery(Query query, Class<T> clazz, RealtimeListener<T> listener) {
        ListenerRegistration registration = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error listening to custom query", error);
                if (listener != null) {
                    listener.onError(error.getMessage());
                }
                return;
            }

            if (value != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    T item = dc.getDocument().toObject(clazz);
                    if (item != null) {
                        // Set ID if the class has setId method
                        try {
                            item.getClass().getMethod("setId", String.class)
                                .invoke(item, dc.getDocument().getReference());
                        } catch (Exception e) {
                            Log.w(TAG, "Could not set ID for " + clazz.getSimpleName());
                        }
                        
                        switch (dc.getType()) {
                            case ADDED:
                                if (listener != null) listener.onDataAdded(item);
                                break;
                            case MODIFIED:
                                if (listener != null) listener.onDataModified(item);
                                break;
                            case REMOVED:
                                if (listener != null) listener.onDataRemoved(item);
                                break;
                        }
                    }
                }
            }
        });
        
        listeners.add(registration);
        return registration;
    }

    // Remove specific listener
    public void removeListener(ListenerRegistration registration) {
        if (registration != null) {
            registration.remove();
            listeners.remove(registration);
        }
    }

    // Remove all listeners
    public void removeAllListeners() {
        for (ListenerRegistration registration : listeners) {
            if (registration != null) {
                registration.remove();
            }
        }
        listeners.clear();
    }

    // Get active listeners count
    public int getActiveListenersCount() {
        return listeners.size();
    }
} 