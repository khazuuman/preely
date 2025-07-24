package com.example.preely.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Service;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ServiceViewModel extends ViewModel {
    private final MutableLiveData<List<Service>> serviceListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION = "services";

    public LiveData<List<Service>> getServiceList() {
        return serviceListLiveData;
    }

    public void loadServices() {
        db.collection(COLLECTION).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Service> list = new ArrayList<>();
            for (var doc : queryDocumentSnapshots.getDocuments()) {
                Service s = doc.toObject(Service.class);
                if (s != null) s.setId(doc.getId());
                list.add(s);
            }
            serviceListLiveData.setValue(list);
        });
    }

    public void addService(Service service) {
        db.collection(COLLECTION).add(service).addOnSuccessListener(documentReference -> loadServices());
    }

    public void updateService(Service service) {
        if (service.getId() == null) return;
        db.collection(COLLECTION).document(service.getId()).set(service).addOnSuccessListener(aVoid -> loadServices());
    }

    public void deleteService(Service service) {
        if (service.getId() == null) return;
        db.collection(COLLECTION).document(service.getId()).delete().addOnSuccessListener(aVoid -> loadServices());
    }

    // Optional: Realtime update
    public void listenRealtime() {
        db.collection(COLLECTION).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) return;
                List<Service> list = new ArrayList<>();
                for (var doc : value.getDocuments()) {
                    Service s = doc.toObject(Service.class);
                    if (s != null) s.setId(doc.getId());
                    list.add(s);
                }
                serviceListLiveData.setValue(list);
            }
        });
    }
} 