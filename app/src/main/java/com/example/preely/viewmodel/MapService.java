package com.example.preely.viewmodel;

import androidx.lifecycle.ViewModel;
import com.example.preely.model.entities.User;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.Constraints;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapService extends ViewModel {
    private MainRepository<User> userRepo = new MainRepository<>(User.class, Constraints.CollectionName.USERS);

    public void updateUserLocation(String userId, GeoPoint newLocation, OnUpdateListener l) {
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .document(userId);

        userRef.update("location", newLocation)
                .addOnSuccessListener(a -> {
                    if (l != null) l.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (l != null) l.onFailure(e.getMessage());
                });
    }

    public interface OnUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }
}
