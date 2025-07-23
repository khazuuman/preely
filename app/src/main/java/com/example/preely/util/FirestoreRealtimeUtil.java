package com.example.preely.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.preely.model.entities.BaseEntity;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Transaction;
import com.example.preely.model.entities.User;
import com.example.preely.view.CustomToast;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreRealtimeUtil {

    private static final String TAG = "FirestoreRealtimeUtil";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<ListenerRegistration> listeners = new ArrayList<>();
    private Context context;

    public FirestoreRealtimeUtil(Context context) {
        this.context = context;
    }

    public FirestoreRealtimeUtil() {
        this.context = null;
    }

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
                            user.setId(dc.getDocument().getId());

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
                            transaction.setId(dc.getDocument().getId());
                            
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
                            category.setId(dc.getDocument().getId());
                            
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

    // Custom query listener
    public <T> ListenerRegistration listenToCustomQuery(Query query, Class<T> clazz, RealtimeListener<T> listener) {
        ListenerRegistration registration = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error listening to custom query", error);
                if (error.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION && error.getMessage().contains("index")) {
                    String indexUrl = extractUrlFromError(error.getMessage());

                    if (context != null) {
                        CustomToast.makeText(context, "Query cần index. Tạo tại: " + indexUrl,
                                CustomToast.LENGTH_LONG, Constraints.NotificationType.ERROR).show();
                    } else {
                        Log.e(TAG, "Query cần index: " + indexUrl);
                    }
                }

                if (listener != null) {
                    listener.onError(error.getMessage());
                }
                return;
            }

            if (value != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    T item = dc.getDocument().toObject(clazz);
                    if (item != null) {
                        try {
                            if (item instanceof BaseEntity) {
                                ((BaseEntity) item).setId(dc.getDocument().getId());
                            }
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

    private String extractUrlFromError(String errorMessage) {
        // Giả sử message có "You can create it here: [url]"
        int start = errorMessage.indexOf("https://");
        if (start != -1) {
            return errorMessage.substring(start);
        }
        return "Firebase Console Indexes";
    }
} 