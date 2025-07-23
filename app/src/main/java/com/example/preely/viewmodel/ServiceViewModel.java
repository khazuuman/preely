package com.example.preely.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.preely.model.entities.Service;
import java.util.ArrayList;
import java.util.List;

public class ServiceViewModel extends ViewModel {
    private final MutableLiveData<List<Service>> serviceListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Service> selectedServiceLiveData = new MutableLiveData<>();

    public LiveData<List<Service>> getServiceList() {
        return serviceListLiveData;
    }

    public LiveData<Service> getSelectedService() {
        return selectedServiceLiveData;
    }

    public void loadServices() {
        // TODO: Load từ repository/database
        List<Service> demoList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Service s = new Service();
            s.setTitle("Service " + i);
            s.setDescription("Description for service " + i);
            s.setPrice(100.0 + i);
            // TODO: set provider_id, category_id nếu cần
            demoList.add(s);
        }
        serviceListLiveData.setValue(demoList);
    }

    public void selectService(Service service) {
        selectedServiceLiveData.setValue(service);
    }

    public void addService(Service service) {
        List<Service> current = serviceListLiveData.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(service);
        serviceListLiveData.setValue(current);
    }

    public void updateService(Service service) {
        List<Service> current = serviceListLiveData.getValue();
        if (current == null) return;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId().equals(service.getId())) {
                current.set(i, service);
                break;
            }
        }
        serviceListLiveData.setValue(current);
    }

    public void deleteService(Service service) {
        List<Service> current = serviceListLiveData.getValue();
        if (current == null) return;
        current.remove(service);
        serviceListLiveData.setValue(current);
    }
} 