package com.example.preely.viewmodel;

import android.util.Log;
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

    // Interface chính để lấy cả fullName và username
    public interface UserInfoCallback {
        void onSuccess(String fullName, String username);
        void onFailure(String error);
    }

    // Interface cũ để backward compatibility
    public interface UserNameCallback {
        void onSuccess(String userName);
        void onFailure(String error);
    }

    // Interface cho tìm kiếm user
    public interface UserSearchCallback {
        void onSuccess(List<UserResponse> users);
        void onFailure(String error);
    }

    /**
     * Lấy thông tin đầy đủ của user (cả fullName và username)
     * Đây là method chính được dùng trong chat
     */
    public void getUserInfo(String userId, UserInfoCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure("User ID is null or empty");
            return;
        }

        // Query user từ Firestore
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .document(userId);

        Query query = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .whereEqualTo("__name__", userRef);

        repository.getOne(query).observeForever(user -> {
            if (user != null) {
                // Ưu tiên full_name, fallback về username
                String fullName = user.getFull_name() != null && !user.getFull_name().isEmpty()
                        ? user.getFull_name()
                        : "Unknown Name";

                String username = user.getUsername() != null && !user.getUsername().isEmpty()
                        ? user.getUsername()
                        : "unknown";

                Log.d("UserService", "User info fetched - Full name: " + fullName + ", Username: " + username + " for ID: " + userId);

                if (callback != null) {
                    callback.onSuccess(fullName, username);
                }
            } else {
                Log.e("UserService", "User not found for ID: " + userId);
                if (callback != null) callback.onFailure("User not found");
            }
        });
    }

    /**
     * Method cũ - chỉ lấy tên để hiển thị (backward compatibility)
     */
    public void getUserName(String userId, UserNameCallback callback) {
        getUserInfo(userId, new UserInfoCallback() {
            @Override
            public void onSuccess(String fullName, String username) {
                // Ưu tiên full name cho display
                String displayName = fullName != null && !fullName.equals("Unknown Name")
                        ? fullName : username;
                if (callback != null) callback.onSuccess(displayName);
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    /**
     * Lấy user info bằng DocumentReference
     */
    public void getUserByRef(DocumentReference userRef, UserNameCallback callback) {
        if (userRef == null) {
            if (callback != null) callback.onFailure("User reference is null");
            return;
        }
        getUserInfo(userRef.getId(), (UserInfoCallback) callback);
    }

    /**
     * Tìm kiếm user theo cả username và full_name
     */
    public void searchUsers(String searchTerm, UserSearchCallback callback) {
        // Query theo username
        Query usernameQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .orderBy("username")
                .startAt(searchTerm.toLowerCase())
                .endAt(searchTerm.toLowerCase() + "\uf8ff")
                .limit(10);

        // Query theo full_name
        Query fullNameQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .orderBy("full_name")
                .startAt(searchTerm.toLowerCase())
                .endAt(searchTerm.toLowerCase() + "\uf8ff")
                .limit(10);

        repository.getList(usernameQuery, null, null).observeForever(usernameResults -> {
            repository.getList(fullNameQuery, null, null).observeForever(fullNameResults -> {
                try {
                    List<UserResponse> combinedResults = new ArrayList<>();

                    // Xử lý kết quả từ username query
                    if (usernameResults != null) {
                        for (User user : usernameResults) {
                            UserResponse response = mapUserToResponse(user);
                            if (!containsUser(combinedResults, response)) {
                                combinedResults.add(response);
                            }
                        }
                    }

                    // Xử lý kết quả từ full_name query
                    if (fullNameResults != null) {
                        for (User user : fullNameResults) {
                            UserResponse response = mapUserToResponse(user);
                            if (!containsUser(combinedResults, response)) {
                                combinedResults.add(response);
                            }
                        }
                    }

                    Log.d("UserService", "Search completed. Found " + combinedResults.size() + " users for term: " + searchTerm);

                    if (callback != null) {
                        callback.onSuccess(combinedResults);
                    }
                } catch (Exception e) {
                    Log.e("UserService", "Error in search: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
        });
    }

    // Helper methods
    private boolean containsUser(List<UserResponse> users, UserResponse user) {
        for (UserResponse existingUser : users) {
            if (existingUser.getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFull_name(user.getFull_name());
        response.setEmail(user.getEmail());
        // Map thêm các trường khác nếu cần
        return response;
    }
}
