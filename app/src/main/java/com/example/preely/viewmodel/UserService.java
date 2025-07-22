package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.User;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.Constraints;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class UserService extends ViewModel {
    private MainRepository<User> repository = new MainRepository<>(User.class, Constraints.CollectionName.USERS);
    private Map<String, String> userNameCache = new HashMap<>();

    public interface UserNameCallback {
        void onSuccess(String userName);
        void onFailure(String error);
    }

    // Method để lấy tên user theo ID
    public void getUserName(String userId, UserNameCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure("User ID is null or empty");
            return;
        }

        if (userNameCache.containsKey(userId)) {
            String cachedName = userNameCache.get(userId);
            Log.d("UserService", "Using cached name: " + cachedName + " for ID: " + userId);
            if (callback != null) callback.onSuccess(cachedName);
            return;
        }

        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("user")
                .document(userId);

        Query query = FirebaseFirestore.getInstance().collection("user")
                .whereEqualTo("__name__", userRef);

        repository.getOne(query).observeForever(user -> {
            if (user != null) {
                String userName = user.getFull_name() != null && !user.getFull_name().isEmpty()
                        ? user.getFull_name()
                        : user.getUsername();

                String finalUserName = userName != null ? userName : "Unknown User";

                userNameCache.put(userId, finalUserName);

                if (callback != null) {
                    callback.onSuccess(finalUserName);
                }

                Log.d("UserService", "User name fetched and cached: " + finalUserName + " for ID: " + userId);
            } else {
                if (callback != null) callback.onFailure("User not found");
                Log.e("UserService", "User not found for ID: " + userId);
            }
        });
    }

    // Method để lấy user theo DocumentReference
    public void getUserByRef(DocumentReference userRef, UserNameCallback callback) {
        if (userRef == null) {
            if (callback != null) callback.onFailure("User reference is null");
            return;
        }

        getUserName(userRef.getId(), callback);
    }
}
