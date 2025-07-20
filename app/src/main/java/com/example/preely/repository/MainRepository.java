package com.example.preely.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.preely.model.entities.BaseEntity;
import com.example.preely.util.CallBackUtil;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.tasks.Tasks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainRepository<T extends BaseEntity> {
    private FirebaseFirestore db;
    private String collectionName;
    private Class<T> modelCl;

    public MainRepository(Class<T> modelCl, String collectionName) {
        this.db = FirebaseFirestore.getInstance();
        this.collectionName = collectionName;
        this.modelCl = modelCl;
    }

    public LiveData<T> getById(Query query) {
        MutableLiveData<T> result = new MutableLiveData<>();
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                result.setValue(doc.toObject(modelCl));
            } else {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<List<T>> getAll(Query query) {
        MutableLiveData<List<T>> result = new MutableLiveData<>();
        List<T> resultList = new ArrayList<>();
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    resultList.add(document.toObject(modelCl));
                }
                result.setValue(resultList);
            } else {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<T> getOne(Query query) {
        MutableLiveData<T> result = new MutableLiveData<>();
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> resultList = task.getResult().getDocuments();
                if (!resultList.isEmpty()) {
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    result.setValue(doc.toObject(modelCl));
                } else {
                    result.setValue(null);
                }
            } else {
                result.setValue(null);
            }
        });
        return result;
    }

    // Retrieve a list of documents with optional filter, ordering, and pagination
    public LiveData<List<T>> getList(Query query, Integer pageSize, Integer pageNumber) {
        MutableLiveData<List<T>> result = new MutableLiveData<>();
        if (pageSize != null && pageNumber != null && pageSize > 0 && pageNumber > 0) {
            query = query.limit(pageSize).startAfter((pageNumber - 1) * pageSize);
        }
        List<T> resultList = new ArrayList<>();
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    resultList.add(document.toObject(modelCl));
                }
                result.setValue(resultList);
            } else {
                result.setValue(null);
            }
        });
        return result;
    }

    // Insert a single document
    public void add(T object, String collectionName, CallBackUtil.OnInsertCallback callback) {
        db.collection(collectionName)
                .add(object)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null) {
                        callback.onSuccess(documentReference);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    // Insert multiple documents using a batch
    public void addRange(List<T> objects, CallBackUtil.OnInsertManyCallback callback) {
        WriteBatch batch = db.batch();
        CollectionReference collectionRef = db.collection(getCollectionName());
        try {
            for (T object : objects) {
                DocumentReference docRef = collectionRef.document();
                batch.set(docRef, object);
            }
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e);
                    });
        } catch (Exception e) {
            Log.e("addRange", "Exception while batching", e);
            if (callback != null) callback.onFailure(e);
        }
    }

    // Update a single document
    public void update(T object, String documentId, CallBackUtil.OnUpdateCallback callback) {
        DocumentReference docRef = db.collection(getCollectionName()).document(String.valueOf(documentId));
        docRef.set(object)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    // Update multiple documents using a batch
    public void updateRange(Map<String, T> entities, CallBackUtil.OnUpdateCallback callback) {
        WriteBatch batch = db.batch();
        try {
            for (Map.Entry<String, T> entry : entities.entrySet()) {
                String id = entry.getKey();
                T object = entry.getValue();
                batch.set(db.collection(getCollectionName()).document(id), object);
            }
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e);
                    });
        } catch (Exception e) {
            if (callback != null) callback.onFailure(e);
        }
    }


    // Delete a single document
    public void delete(String documentId, CallBackUtil.OnDeleteCallBack callBack) {
        db.collection(getCollectionName()).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callBack != null) callBack.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callBack != null) callBack.onFailure(e);
                });
    }


    // Delete multiple documents using a batch
    public void deleteRange(List<String> documentIds, CallBackUtil.OnDeleteCallBack callBack) {
        WriteBatch batch = db.batch();
        for (String documentId : documentIds) {
            batch.delete(db.collection(getCollectionName()).document(documentId));
        }
        batch.commit()
                .addOnSuccessListener(unused -> {
                    if (callBack != null) callBack.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callBack != null) callBack.onFailure(e);
                });
    }

    // Get count of documents with optional filter
    public LiveData<Integer> getCount(Query query) {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        query.count().get(AggregateSource.SERVER)
                .addOnSuccessListener(aggregateQuerySnapshot -> {
                    long countL = aggregateQuerySnapshot.getCount();
                    result.setValue(Integer.parseInt(String.valueOf(countL)));
                });
        return result;
    }

    // Emulate transaction start (Firestore uses WriteBatch for batch operations)
    public WriteBatch beginTransaction() {
        return db.batch();
    }

    // Commit transaction
    public void commitTransaction(WriteBatch batch) {
        try {
            Tasks.await(batch.commit());
            Log.d("Success", "Transaction successfully committed!");
        } catch (Exception e) {
            Log.e("Error", "Error committing transaction", e);
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }
}