package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.authentication.SessionManager;
import com.example.preely.model.dto.UserDto;
import com.example.preely.model.request.UserRequest;
import com.example.preely.model.response.UserResponse;
import com.example.preely.repository.UserRepository;
import com.example.preely.util.FirestoreCallback;

public class UserViewModel extends ViewModel {

    FirestoreCallback callback;
    private final UserRepository userRepository = new UserRepository();
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<UserResponse> loginResult = new MutableLiveData<>();
    public LiveData<String> getUsernameError() {
        return usernameError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<UserResponse> getLoginResult() {
        return loginResult;
    }

    public boolean validate(UserRequest request) {
        boolean isValid = true;
        if (request.getUsername().isEmpty() || request.getUsername().trim().isEmpty()) {
            usernameError.setValue("Username is not be empty");
            isValid = false;
        } else {
            usernameError.setValue(null);
        }
        if (request.getPassword().isEmpty()) {
            passwordError.setValue("Password is not be empty");
            isValid = false;
        } else {
            passwordError.setValue(null);
        }
        return isValid;
    }

    public void loginByUsername(UserRequest request) {
        if (!validate(request)) {
            return;
        }
        userRepository.loginByUserName(request).observeForever(userResponse -> {
            if (userResponse != null) {
                Log.i("user info", userResponse.getId());
                loginResult.setValue(userResponse);
            } else {
                loginResult.setValue(null);
            }
        });
    }
}
