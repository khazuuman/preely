package com.example.preely.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.preely.model.dto.CommonDto;
import com.example.preely.model.dto.UserDto;
import com.example.preely.model.request.UserRequest;
import com.example.preely.model.response.UserResponse;
import com.example.preely.util.Constraints.*;
import com.example.preely.util.DataUtil;
import com.example.preely.util.DbUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserRepository extends MainRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public UserRepository() {
        this.setTableName(CollectionName.USERS);
    }

    public LiveData<Boolean> registerByUserName(UserRequest user) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        db.collection(getTableName()).whereEqualTo("username", user.getUsername())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        String hashPassword = DataUtil.hashPassword(user.getPassword());
                        user.setPassword(hashPassword);
                        insert(user);
                    } else {
                        result.setValue(false);
                    }
                });
        return result;
    }

    public LiveData<UserResponse> loginByUserName(UserRequest request) {
        MutableLiveData<UserResponse> result = new MutableLiveData<>();
        db.collection(getTableName())
                .whereEqualTo("username", request.getUsername())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        if (DataUtil.checkPassword(request.getPassword(), document.getString("encode_password"))) {
                            result.setValue(document.toObject(UserResponse.class));
                        } else {
                            result.setValue(null);
                        }
                    } else {
                        result.setValue(null);
                    }
                });
        return result;
    }
}
