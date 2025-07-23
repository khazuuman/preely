package com.example.preely.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.preely.model.entities.Service;
import java.util.ArrayList;
import java.util.List;

public class SavedServiceViewModel extends ViewModel {
    private final MutableLiveData<List<Service>> savedServiceListLiveData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Service>> getSavedServiceList() {
        return savedServiceListLiveData;
    }

    public void loadSavedServices() {
        // TODO: Load từ repository/database
        List<Service> demoList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Service s = new Service();
            s.setTitle("Saved Service " + i);
            s.setDescription("Description for saved service " + i);
            s.setPrice(150.0 + i);
            s.setAvailability("Weekends");
            demoList.add(s);
        }
        savedServiceListLiveData.setValue(demoList);
    }

    public void addSavedService(Service service) {
        List<Service> current = savedServiceListLiveData.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(service);
        savedServiceListLiveData.setValue(current);
    }

    public void removeSavedService(Service service) {
        List<Service> current = savedServiceListLiveData.getValue();
        if (current == null) return;
        current.remove(service);
        savedServiceListLiveData.setValue(current);
    }
} 