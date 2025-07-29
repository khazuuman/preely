package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Skill;
import com.example.preely.model.entities.User;
import com.example.preely.model.request.UserLoginRequest;
import com.example.preely.model.request.UserRegisterRequest;
import com.example.preely.model.response.SkillResponse;
import com.example.preely.model.response.UserResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.DataUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.preely.util.Constraints.*;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true)
public class UserLoginService extends ViewModel {
    public final MainRepository<User> userRepository = new MainRepository<>(User.class, CollectionName.USERS);
    public final MainRepository<Skill> skillRepository = new MainRepository<>(Skill.class, CollectionName.SKILL);
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
            userInfo.setValue(null);
            return;
        }
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("username", request.getUsername())
                .whereEqualTo("provider", AccountType.LOCAL);

        userRepository.getOne(query).observeForever(user -> {
            if (user != null && DataUtil.checkPassword(request.getPassword(), user.getEncode_password())) {
                user.setLast_login(Timestamp.now());
                user.setUpdate_at(Timestamp.now());
                userRepository.update(user, user.getId(), new CallBackUtil.OnUpdateCallback() {
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
                    UserResponse userResponse = DataUtil.mapObj(user, UserResponse.class);
                    getUserSkillList(user.getSkill_ids(), new CallBackUtil.SkillListCallback() {
                        @Override
                        public void onSuccess(List<SkillResponse> skillResponses) {
                            userResponse.setSkills(skillResponses);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            userResponse.setSkills(null);
                        }
                    });

                    userInfo.setValue(userResponse);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.e("USER", "Failed to get user");
                userInfo.setValue(null);
            }
        });
    }


    private void checkUsernameExist(String username) {
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("username", username)
                .whereEqualTo("provider", AccountType.LOCAL)
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
                user.setProvider(AccountType.LOCAL);
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
                .whereEqualTo("provider", AccountType.GOOGLE)
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
                newUser.setAvatar(String.valueOf(userFb.getPhotoUrl()));
                newUser.setProvider(AccountType.GOOGLE);
                userRepository.add(newUser, CollectionName.USERS, new CallBackUtil.OnInsertCallback() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        getAccountInfo(newUser.getEmail(), AccountType.GOOGLE);
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
                userRepository.update(user, user.getId(), new CallBackUtil.OnUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        getAccountInfo(user.getEmail(), AccountType.GOOGLE);
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

    public void getAccountInfo(String id, String accType) {
        Query query = null;
        if (accType.equals(AccountType.GOOGLE)) {
            query = FirebaseFirestore.getInstance()
                    .collection(CollectionName.USERS)
                    .whereEqualTo("email", id)
                    .whereEqualTo("provider", AccountType.GOOGLE)
                    .limit(1);
        } else if (accType.equals(AccountType.TWITTER)) {
            query = FirebaseFirestore.getInstance()
                    .collection(CollectionName.USERS)
                    .whereEqualTo("username", id)
                    .whereEqualTo("provider", AccountType.TWITTER)
                    .limit(1);
        }
        assert query != null;
        userRepository.getOne(query).observeForever(user -> {
            if (user != null) {
                try {
                    UserResponse userResponse = DataUtil.mapObj(user, UserResponse.class);
                    getUserSkillList(user.getSkill_ids(), new CallBackUtil.SkillListCallback() {
                        @Override
                        public void onSuccess(List<SkillResponse> skillResponses) {
                            userResponse.setSkills(skillResponses);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            userResponse.setSkills(null);
                        }
                    });

                    userInfo.setValue(userResponse);
                    Log.i("GET USER", user.toString());
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            } else {
                userInfo.setValue(null);
            }
        });
    }

    public void handleTwitterLoginDetail(FirebaseUser userFb) {
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.USERS)
                .whereEqualTo("username", userFb.getUid())
                .whereEqualTo("provider", AccountType.TWITTER)
                .limit(1);
        userRepository.getOne(query).observeForever(user -> {
            if (user == null) {
                User newUser = new User();
                newUser.set_active(true);
                newUser.setCreate_at(Timestamp.now());
                newUser.setRegistration_date(Timestamp.now());
                newUser.setFull_name(userFb.getDisplayName());
                newUser.setUsername(userFb.getUid());
                newUser.setAvatar(String.valueOf(userFb.getPhotoUrl()));
                newUser.setProvider(AccountType.TWITTER);
                userRepository.add(newUser, CollectionName.USERS, new CallBackUtil.OnInsertCallback() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        getAccountInfo(newUser.getUsername(), AccountType.TWITTER);
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
                userRepository.update(user, user.getId(), new CallBackUtil.OnUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        getAccountInfo(user.getUsername(), AccountType.TWITTER);
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

    public void getUserSkillList(List<DocumentReference> skillRefs, CallBackUtil.SkillListCallback callback) {
        if (skillRefs == null || skillRefs.isEmpty()) {
            callback.onSuccess(null);
            return;
        }
        List<String> skillIds = new ArrayList<>();
        for (DocumentReference ref : skillRefs) {
            skillIds.add(ref.getId()); // lấy documentId
        }
        FirebaseFirestore.getInstance()
                .collection(CollectionName.SKILL)
                .whereIn(FieldPath.documentId(), skillIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Skill> results = queryDocumentSnapshots.toObjects(Skill.class);
                    List<SkillResponse> skills = new ArrayList<>();
                    for (Skill skill : results) {
                        try {
                            SkillResponse skillResponse = DataUtil.mapObj(skill, SkillResponse.class);
                            skills.add(skillResponse);
                        } catch (IllegalAccessException | InstantiationException e) {
                            e.printStackTrace();
                        }
                    }
                    callback.onSuccess(skills);
                })
                .addOnFailureListener(callback::onFailure);
    }



}