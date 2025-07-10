package com.example.preely.repository;

import android.util.Log;
import com.example.preely.util.FirestoreCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MainRepository<T> {
    private final FirebaseFirestore db;
    private final String tableName;
    private final Class<T> modelClass;

    public MainRepository(String tableName, Class<T> modelClass) {
        this.db = FirebaseFirestore.getInstance();
        this.tableName = tableName;
        this.modelClass = modelClass;
    }

    // Getters
    public String getTableName() {
        return tableName;
    }

    public Class<T> getModelClass() {
        return modelClass;
    }

    // Retrieve a single document by ID
    public T getById(String id) {
        try {
            DocumentSnapshot document = Tasks.await(db.collection(tableName).document(id).get());
            if (document.exists()) {
                return document.toObject(modelClass);
            }
            return null;
        } catch (Exception e) {
            Log.e("Error", "Error getting document by ID: " + id, e);
            throw new RuntimeException("Failed to get document by ID: " + id, e);
        }
    }

    public List<T> getAll(FirestoreCallback<T> callback) {
        List<T> resultList = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot document : Tasks.await(db.collection(tableName).get())) {
                resultList.add(document.toObject(modelClass));
            }
            if (callback != null) {
                callback.onCallback(resultList);
            }
            return resultList;
        } catch (Exception e) {
            Log.e("Error", "Error getting documents", e);
            if (callback != null) {
                callback.onCallback(resultList);
            }
            throw new RuntimeException("Failed to get all documents", e);
        }
    }

    public T getOne(Function<Query, Query> queryBuilder) {
        try {
            Query query = db.collection(tableName);
            if (queryBuilder != null) {
                query = queryBuilder.apply(query);
            }
            List<DocumentSnapshot> documents = Tasks.await(query.limit(1).get()).getDocuments();
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(modelClass);
            }
            return null;
        } catch (Exception e) {
            Log.e("Error", "Error getting one document", e);
            throw new RuntimeException("Failed to get one document", e);
        }
    }

    // Retrieve a list of documents with optional filter, ordering, and pagination
    public List<T> getList(Function<Query, Query> queryBuilder, Integer pageSize, Integer pageNumber) {
        try {
            Query query = db.collection(tableName);
            if (queryBuilder != null) {
                query = queryBuilder.apply(query);
            }
            if (pageSize != null && pageNumber != null && pageSize > 0 && pageNumber > 0) {
                query = query.limit(pageSize).startAfter((pageNumber - 1) * pageSize);
            }
            List<T> resultList = new ArrayList<>();
            for (QueryDocumentSnapshot document : Tasks.await(query.get())) {
                resultList.add(document.toObject(modelClass));
            }
            return resultList;
        } catch (Exception e) {
            Log.e("Error", "Error getting document list", e);
            throw new RuntimeException("Failed to get document list", e);
        }
    }

    // Insert a single document
    public void add(T entity) {
        try {
            WriteBatch batch = db.batch();
            batch.set(db.collection(tableName).document(), entity);
            Tasks.await(batch.commit());
            Log.d("Success", "DocumentSnapshot successfully added!");
        } catch (Exception e) {
            Log.e("Error", "Error adding document", e);
            throw new RuntimeException("Failed to add document", e);
        }
    }

    // Insert multiple documents using a batch
    public void addRange(List<T> entities) {
        try {
            WriteBatch batch = db.batch();
            for (T entity : entities) {
                batch.set(db.collection(tableName).document(), entity);
            }
            Tasks.await(batch.commit());
            Log.d("Success", "DocumentSnapshots successfully added!");
        } catch (Exception e) {
            Log.e("Error", "Error adding document range", e);
            throw new RuntimeException("Failed to add document range", e);
        }
    }

    // Update a single document
    public void update(T entity, String documentId) {
        try {
            Tasks.await(db.collection(tableName).document(documentId).set(entity));
            Log.d("Success", "DocumentSnapshot successfully updated!");
        } catch (Exception e) {
            Log.e("Error", "Error updating document", e);
            throw new RuntimeException("Failed to update document", e);
        }
    }

    // Update multiple documents using a batch
    public void updateRange(Map<String, T> entities) {
        try {
            WriteBatch batch = db.batch();
            entities.forEach((id, entity) -> batch.set(db.collection(tableName).document(id), entity));
            Tasks.await(batch.commit());
            Log.d("Success", "DocumentSnapshots successfully updated!");
        } catch (Exception e) {
            Log.e("Error", "Error updating document range", e);
            throw new RuntimeException("Failed to update document range", e);
        }
    }

    // Delete a single document
    public void delete(String documentId) {
        try {
            Tasks.await(db.collection(tableName).document(documentId).delete());
            Log.d("Success", "DocumentSnapshot successfully deleted!");
        } catch (Exception e) {
            Log.e("Error", "Error deleting document", e);
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    // Delete multiple documents using a batch
    public void deleteRange(List<String> documentIds) {
        try {
            WriteBatch batch = db.batch();
            for (String id : documentIds) {
                batch.delete(db.collection(tableName).document(id));
            }
            Tasks.await(batch.commit());
            Log.d("Success", "DocumentSnapshots successfully deleted!");
        } catch (Exception e) {
            Log.e("Error", "Error deleting document range", e);
            throw new RuntimeException("Failed to delete document range", e);
        }
    }

    // Get count of documents with optional filter
    public int getCount(Function<Query, Query> queryBuilder) {
        try {
            Query query = db.collection(tableName);
            if (queryBuilder != null) {
                query = queryBuilder.apply(query);
            }
            return Tasks.await(query.get()).size();
        } catch (Exception e) {
            Log.e("Error", "Error getting document count", e);
            throw new RuntimeException("Failed to get document count", e);
        }
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