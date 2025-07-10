package com.example.preely.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.preely.model.dto.UserDto;
import com.example.preely.model.request.UserRequest;
import com.example.preely.model.response.UserResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.DataUtil;
import com.google.firebase.firestore.Query;

import java.util.function.Function;

public class LoginService extends ViewModel {
    private final MainRepository<UserDto> userRepository;
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<UserResponse> loginResult = new MutableLiveData<>();

    public LoginService() {
        this.userRepository = new MainRepository<>("User", UserDto.class);
    }

    public LiveData<String> getUsernameError() {
        return usernameError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<UserResponse> getLoginResult() {
        return loginResult;
    }

    /**
     * Validates the login request by checking if username and password are non-empty.
     *
     * @param request The UserRequest containing username and password.
     * @return True if valid, false otherwise.
     */
    public boolean validate(UserRequest request) {
        boolean isValid = true;
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            usernameError.setValue("Username cannot be empty");
            isValid = false;
        } else {
            usernameError.setValue(null);
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            passwordError.setValue("Password cannot be empty");
            isValid = false;
        } else {
            passwordError.setValue(null);
        }
        return isValid;
    }

    /**
     * Attempts to log in a user by querying Firestore for a matching username and verifying
     * the password against a stored hash. Runs the query in a background thread to avoid blocking.
     *
     * @param request The UserRequest containing username and password.
     */
    public void loginByUsername(UserRequest request) {
        if (!validate(request)) {
            loginResult.setValue(null);
            return;
        }

        // Run Firestore query in a background thread to avoid blocking
        new Thread(() -> {
            try {
                // Build a query to find a user with matching username
                Function<Query, Query> queryBuilder = query ->
                        query.whereEqualTo("username", request.getUsername());

                // Use MainRepository's getOne to find the matching user
                UserDto user = userRepository.getOne(queryBuilder);
                // Post result to main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (user != null && DataUtil.checkPassword(request.getPassword(), user.getEncode_password())) {
                        // Map UserDto to UserResponse
                        UserResponse response = new UserResponse();
                        response.setId(user.getId());
                        response.setUsername(user.getUsername());
                        loginResult.setValue(response);
                        Log.i("LoginService", "Login successful, ID: " + response.getId());
                    } else {
                        loginResult.setValue(null);
                        Log.i("LoginService", "Login failed: Invalid username or password");
                    }
                });
            } catch (Exception e) {
                Log.e("LoginService", "Error during login: " + e.getMessage(), e);
                // Post null result to main thread
                new Handler(Looper.getMainLooper()).post(() -> loginResult.setValue(null));
            }
        }).start();
    }
}