package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.dto.UserDto;
import com.example.preely.model.request.UserRequest;
import com.example.preely.model.response.UserResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.DataUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.preely.util.Constraints.*;

import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true)
public class LoginService extends ViewModel {
    public final MainRepository<UserDto> userRepository = new MainRepository<>(UserDto.class);
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

    public void loginByUsername(UserRequest request) {
        if (!validate(request)) {
            loginResult.setValue(null);
            return;
        }

        // Check for admin credentials first
        if ("Admin".equals(request.getUsername()) && "Admin@123".equals(request.getPassword())) {
            // Create admin user response
            UserResponse adminUser = new UserResponse();
            adminUser.setId("admin");
            adminUser.setUsername("Admin");
            adminUser.setFull_fame("Administrator");
            adminUser.setEmail("admin@preely.com");
            adminUser.setPhone_number("0000000000");
            loginResult.setValue(adminUser);
            return;
        }

        // Check database for regular users
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("username", request.getUsername());

        userRepository.getOne(query).observeForever(user -> {
            if (user != null && DataUtil.checkPassword(request.getPassword(), user.getEncode_password())) {
                Log.i("User info", user.getId());
                loginResult.setValue(DataUtil.mapObj(user, UserResponse.class));
            } else {
                loginResult.setValue(null);
            }
        });
    }
}