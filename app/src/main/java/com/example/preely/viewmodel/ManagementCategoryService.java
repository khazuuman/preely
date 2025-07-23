package com.example.preely.viewmodel;

import androidx.lifecycle.Observer;
import com.example.preely.model.entities.Category;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import java.util.List;

public class ManagementCategoryService {
    private final MainRepository<Category> categoryRepository = new MainRepository<>(Category.class, "category");
    private final FirestoreRealtimeUtil realtimeUtil = new FirestoreRealtimeUtil();
    private ListenerRegistration categoryListener;

    public void getAllCategories(Observer<List<Category>> observer) {
        Query query = FirebaseFirestore.getInstance().collection("category");
        categoryRepository.getAll(query).observeForever(observer);
    }

    public void addCategory(Category category, CallBackUtil.OnInsertCallback callback) {
        categoryRepository.add(category, "category", callback);
    }

    public void updateCategory(Category category, CallBackUtil.OnUpdateCallback callback) {
        categoryRepository.update(category, category.getId(), callback);
    }

    public void deleteCategory(Category category, CallBackUtil.OnDeleteCallBack callback) {
        categoryRepository.delete(category.getId(), callback);
    }

    public void listenRealtime(FirestoreRealtimeUtil.RealtimeListener<Category> listener) {
        categoryListener = realtimeUtil.listenToCategories(listener);
    }

    public void removeRealtimeListener() {
        if (categoryListener != null) categoryListener.remove();
        realtimeUtil.removeAllListeners();
    }
} 