package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.User;
import com.example.preely.model.response.UserResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.Constraints;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService extends ViewModel {
    private MainRepository<User> repository = new MainRepository<>(User.class, Constraints.CollectionName.USERS);
    private Map<String, String> userNameCache = new HashMap<>();

    public interface UserNameCallback {
        void onSuccess(String userName);
        void onFailure(String error);
    }

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

    public void getUserByRef(DocumentReference userRef, UserNameCallback callback) {
        if (userRef == null) {
            if (callback != null) callback.onFailure("User reference is null");
            return;
        }

        getUserName(userRef.getId(), callback);
    }

    // Interface for user search callback
    public interface UserSearchCallback {
        void onSuccess(List<UserResponse> users);
        void onFailure(String error);
    }

    public void searchUsers(String searchTerm, UserSearchCallback callback) {
        Query usernameQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .orderBy("username")
                .startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
                .limit(10);

        Query fullNameQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .orderBy("full_name")
                .startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
                .limit(10);

        repository.getList(usernameQuery, null, null).observeForever(usernameResults -> {
            repository.getList(fullNameQuery, null, null).observeForever(fullNameResults -> {
                try {
                    List<UserResponse> combinedResults = new ArrayList<>();

                    // Process username results
                    if (usernameResults != null) {
                        for (User user : usernameResults) {
                            UserResponse response = mapUserToResponse(user);
                            if (!containsUser(combinedResults, response)) {
                                combinedResults.add(response);
                            }
                        }
                    }

                    // Process full name results
                    if (fullNameResults != null) {
                        for (User user : fullNameResults) {
                            UserResponse response = mapUserToResponse(user);
                            if (!containsUser(combinedResults, response)) {
                                combinedResults.add(response);
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(combinedResults);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
        });
    }

    private boolean containsUser(List<UserResponse> users, UserResponse user) {
        String userId = user.getId().getId();
        for (UserResponse existingUser : users) {
            if (existingUser.getId().getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    private UserResponse mapUserToResponse(User user) throws Exception {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFull_name(user.getFull_name());
        response.setEmail(user.getEmail());
        return response;
    }
}
