package com.example.preely.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.util.Constraints;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ForgotPasswordService extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> phone = new MutableLiveData<>();
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> phoneError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUsernameValid = new MutableLiveData<>();

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public void setPhone(String phone) {
        this.phone.setValue(phone);
    }

    public LiveData<String> getUsername() {
        return username;
    }

    public LiveData<String> getPhone() {
        return phone;
    }

    public LiveData<Boolean> isUsernameValid() {
        return isUsernameValid;
    }
    public LiveData<String> getUsernameError() {
        return usernameError;
    }
    public LiveData<String> getPhoneError() {
        return phoneError;
    }

    public void checkUsername(String username, String phone) {
        if (username == null || username.isEmpty()) {
            usernameError.setValue("Username is required");
            isUsernameValid.setValue(false);
            return;
        }
        if (phone == null || phone.isEmpty()) {
            phoneError.setValue("Phone is required");
            isUsernameValid.setValue(false);
            return;
        }
        Query query = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.USERS).whereEqualTo("username", username);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    usernameError.setValue("Username is not exist");
                    isUsernameValid.setValue(false);
                } else {
                    usernameError.setValue("");
                    isUsernameValid.setValue(true);
                }
            }
        });
    }
}
