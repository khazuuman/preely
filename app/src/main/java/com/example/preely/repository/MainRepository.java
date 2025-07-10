package com.example.preely.repository;

import android.util.Log;

import com.example.preely.util.FirestoreCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class MainRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tableName;

    public <T> void getAll(Class<T> modelClass, FirestoreCallback<T> callback) {
        Log.i("Table name", getTableName());
        List<T> resultList = new ArrayList<>();
        db.collection(getTableName())
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        resultList.add(document.toObject(modelClass));
                    }
                    callback.onCallback(resultList);
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(resultList);
                    Log.e("Error", "Error getting documents", e);
                });
    }

    public void insert(Object object) {
        db.collection(getTableName())
                .add(object)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Success", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "Error adding document", e);
                });
    }

    public void update(Object object, String objectId) {
        db.collection(getTableName()).document(objectId)
                .set(object)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Success", "DocumentSnapshot successfully updated!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Error", "Error updating document", e);
                });
    }

    public void delete(String objectId) {
        db.collection(getTableName()).document(objectId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Success", "DocumentSnapshot successfully deleted!");
                });
    }
}
