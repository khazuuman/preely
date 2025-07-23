package com.example.preely.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ServiceAdapter;
import com.example.preely.model.entities.Service;
import java.util.ArrayList;
import java.util.List;

public class ServiceManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Spinner spinnerCategory, spinnerStatus;
    private ServiceAdapter adapter;
    private List<Service> serviceList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_management, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        adapter = new ServiceAdapter(serviceList, new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onEdit(Service service) {
                // TODO: Xử lý sửa dịch vụ
            }
            @Override
            public void onDelete(Service service) {
                // TODO: Xử lý xóa dịch vụ
            }
            @Override
            public void onItemClick(Service service) {
                // TODO: Xử lý xem chi tiết dịch vụ
            }
        });
        recyclerView.setAdapter(adapter);
        loadServices();
        return view;
    }

    private void loadServices() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Load danh sách service từ ViewModel/Repository
        // Demo dữ liệu mẫu
        serviceList.clear();
        for (int i = 1; i <= 7; i++) {
            Service s = new Service();
            s.setTitle("Service Mgmt " + i);
            s.setDescription("Description for service mgmt " + i);
            s.setPrice(300.0 + i);
            // TODO: set provider_id, category_id nếu cần
            serviceList.add(s);
        }
        adapter.setServiceList(serviceList);
        progressBar.setVisibility(View.GONE);
    }
} 