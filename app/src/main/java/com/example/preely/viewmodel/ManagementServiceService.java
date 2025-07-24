package com.example.preely.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class ManagementServiceService extends ViewModel {
    private final MutableLiveData<List<Service>> serviceListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Category>> categoryListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<User>> userListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String SERVICE_COLLECTION = "service";
    private final String CATEGORY_COLLECTION = "category";
    private final String USER_COLLECTION = "user";

    public LiveData<List<Service>> getServiceList() {
        return serviceListLiveData;
    }
    public LiveData<List<Category>> getCategoryList() {
        return categoryListLiveData;
    }
    public LiveData<List<User>> getUserList() {
        return userListLiveData;
    }

    public void loadServices() {
        db.collection(SERVICE_COLLECTION).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Service> list = new ArrayList<>();
            for (var doc : queryDocumentSnapshots.getDocuments()) {
                Service s = doc.toObject(Service.class);
                if (s != null) {
                    s.setId(doc.getId());
                    // Đồng bộ create_at/update_at nếu thiếu
                    if (s.getCreate_at() == null) s.setCreate_at(null);
                    if (s.getUpdate_at() == null) s.setUpdate_at(null);
                }
                list.add(s);
            }
            serviceListLiveData.setValue(list);
        });
    }
    public void addService(Service service) {
        service.setUpdate_at(Timestamp.now());
        service.setCreate_at(Timestamp.now());
        db.collection(SERVICE_COLLECTION).add(service).addOnSuccessListener(documentReference -> loadServices());
    }
    public void updateService(Service service) {
        if (service.getId() == null) return;
        service.setUpdate_at(Timestamp.now());
        db.collection(SERVICE_COLLECTION).document(service.getId()).set(service).addOnSuccessListener(aVoid -> loadServices());
    }
    public void deleteService(Service service) {
        if (service.getId() == null) return;
        db.collection(SERVICE_COLLECTION).document(service.getId()).delete().addOnSuccessListener(aVoid -> loadServices());
    }
    public void fetchCategories() {
        db.collection(CATEGORY_COLLECTION).get().addOnSuccessListener(categorySnap -> {
            List<Category> categories = new ArrayList<>();
            for (QueryDocumentSnapshot doc : categorySnap) {
                Category c = doc.toObject(Category.class);
                c.setId(doc.getId());
                categories.add(c);
            }
            categoryListLiveData.setValue(categories);
        });
    }
    public void fetchUsers() {
        db.collection(USER_COLLECTION).get().addOnSuccessListener(userSnap -> {
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot doc : userSnap) {
                User u = doc.toObject(User.class);
                u.setId(doc.getId());
                users.add(u);
            }
            userListLiveData.setValue(users);
        });
    }
} 