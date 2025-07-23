package com.example.preely.viewmodel;

import androidx.lifecycle.Observer;
import com.example.preely.model.entities.User;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.List;

public class ManagementUserService {
    private final MainRepository<User> userRepository = new MainRepository<>(User.class, "user");
    private final FirestoreRealtimeUtil realtimeUtil = new FirestoreRealtimeUtil();
    private ListenerRegistration userListener;

    public void getAllUsers(Observer<List<User>> observer) {
        Query query = FirebaseFirestore.getInstance().collection("user");
        userRepository.getAll(query).observeForever(observer);
    }

    public void addUser(User user, CallBackUtil.OnInsertCallback callback) {
        userRepository.add(user, "user", callback);
    }

    public void updateUser(User user, CallBackUtil.OnUpdateCallback callback) {
        userRepository.update(user, user.getId(), callback);
    }

    public void deleteUser(User user, CallBackUtil.OnDeleteCallBack callback) {
        userRepository.delete(user.getId(), callback);
    }

    public void listenRealtime(FirestoreRealtimeUtil.RealtimeListener<User> listener) {
        userListener = realtimeUtil.listenToUsers(listener);
    }

    public void removeRealtimeListener() {
        if (userListener != null) userListener.remove();
        realtimeUtil.removeAllListeners();
    }
} 