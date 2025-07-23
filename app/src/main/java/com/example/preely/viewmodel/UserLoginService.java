package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.authentication.SessionManager;
import com.example.preely.model.entities.User;
import com.example.preely.model.request.UserLoginRequest;
import com.example.preely.model.request.UserRegisterRequest;
import com.example.preely.model.response.UserResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.DataUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.preely.util.Constraints.*;

import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true)
public class UserLoginService extends ViewModel {
    public final MainRepository<User> userRepository = new MainRepository<>(User.class, CollectionName.USERS);
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordError = new MutableLiveData<>();
    private final MutableLiveData<UserResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signupResult = new MutableLiveData<>();
    private final MutableLiveData<UserResponse> userInfo = new MutableLiveData<>();
    public LiveData<UserResponse> getUserInfo() {
        return userInfo;
    }

    public LiveData<Boolean> getSignupResult() {
        return signupResult;
    }

    public LiveData<String> getUsernameError() {
        return usernameError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<String> getConfirmPasswordError() {
        return confirmPasswordError;
    }

    public LiveData<UserResponse> getLoginResult() {
        return loginResult;
    }

    private final MutableLiveData<Boolean> isUsernameExist = new MutableLiveData<>();

    public LiveData<Boolean> getIsUsernameExist() {
        return isUsernameExist;
    }

    private boolean loginValidate(UserLoginRequest request) {
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

    public void loginByUsername(UserLoginRequest request) {
        if (!loginValidate(request)) {
            loginResult.setValue(null);
            return;
        }
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("username", request.getUsername());

        userRepository.getOne(query).observeForever(user -> {
            if (user != null && DataUtil.checkPassword(request.getPassword(), user.getEncode_password())) {
                user.setLast_login(Timestamp.now());
                userRepository.update(user, user.getId().getId(), new CallBackUtil.OnUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("UPDATE USER", "User update successfully");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("UPDATE USER", "Failed to update user");
                    }
                });
                try {
                    loginResult.setValue(DataUtil.mapObj(user, UserResponse.class));
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.e("USER", "Failed to get user");
                loginResult.setValue(null);
            }
        });
    }


    private void checkUsernameExist(String username) {
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("username", username)
                .limit(1);

        userRepository.getOne(query).observeForever(user -> {
            isUsernameExist.setValue(user != null);
        });
    }

    private boolean registerValidate(UserRegisterRequest request) {
        boolean isValid = true;
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            usernameError.setValue("Username cannot be empty");
            isValid = false;
        } else if (request.getUsername().length() < 6) {
            usernameError.setValue("Username must be at least 6 characters long");
            isValid = false;
        } else {
            usernameError.setValue(null);
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            passwordError.setValue("Password cannot be empty");
            isValid = false;
        } else if (!DataUtil.isValidPassword(request.getPassword())) {
            passwordError.setValue("Password must contain at least one uppercase letter, one number, and one special character");
            isValid = false;
        } else {
            passwordError.setValue(null);
        }

        if (!request.getConfirmPassword().equals(request.getPassword())) {
            confirmPasswordError.setValue("Passwords do not match");
            isValid = false;
        } else {
            confirmPasswordError.setValue(null);
        }

        return isValid;
    }

    public void registerByUsername(UserRegisterRequest request) {
        checkUsernameExist(request.getUsername());

        getIsUsernameExist().observeForever(exist -> {
            if (exist) {
                usernameError.setValue("Username already exists");
                signupResult.setValue(false);
            } else {
                if (!registerValidate(request)) {
                    signupResult.setValue(false);
                    return;
                }
                User user = new User();
                user.setUsername(request.getUsername());
                user.setEncode_password(DataUtil.hashPassword(request.getPassword()));
                user.set_active(true);
                user.setCreate_at(Timestamp.now());
                user.setRegistration_date(Timestamp.now());

                userRepository.add(user, CollectionName.USERS, new CallBackUtil.OnInsertCallback() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("INSERT USER", "User insert successfully");
                        signupResult.setValue(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("INSERT USER", "Failed to insert user");
                        signupResult.setValue(false);
                    }
                });
            }
        });
    }

    public void handleGoogleLoginDetail(FirebaseUser userFb) {
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("email", userFb.getEmail())
                .limit(1);
        userRepository.getOne(query).observeForever(user -> {
            if (user == null) {
                User newUser = new User();
                newUser.setEmail(userFb.getEmail());
                newUser.set_active(true);
                newUser.setCreate_at(Timestamp.now());
                newUser.setRegistration_date(Timestamp.now());
                newUser.setFull_name(userFb.getDisplayName());
                newUser.setPhone_number(userFb.getPhoneNumber());
                userRepository.add(newUser, CollectionName.USERS, new CallBackUtil.OnInsertCallback() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        getGoogleAccountInfo(newUser.getEmail());
                        Log.i("INSERT USER", "User insert successfully");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("INSERT USER", "Failed to insert user");
                    }
                });
            } else {
                user.setLast_login(Timestamp.now());
                user.setUpdate_at(Timestamp.now());
                userRepository.update(user, user.getId().getId(), new CallBackUtil.OnUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        getGoogleAccountInfo(user.getEmail());
                        Log.i("UPDATE USER", "User update successfully");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("UPDATE USER", "Failed to update user");
                    }

                });
            }
        });
    }

    public void getGoogleAccountInfo(String email) {
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("email", email)
                .limit(1);
        userRepository.getOne(query).observeForever(user -> {
           UserResponse userResponse;
           if (user != null) {
               try {
                   userResponse = DataUtil.mapObj(user, UserResponse.class);
                   userInfo.setValue(userResponse);
               } catch (IllegalAccessException | InstantiationException e) {
                   throw new RuntimeException(e);
               }
           } else {
               userInfo.setValue(null);
           }
        });
    }

}