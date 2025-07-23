package com.example.preely.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ServiceMarketAdapter;
import com.example.preely.model.entities.Service;
import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Spinner spinnerCategory, spinnerSort;
    private ServiceMarketAdapter adapter;
    private List<Service> serviceList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSort = findViewById(R.id.spinnerSort);
        adapter = new ServiceMarketAdapter(serviceList, new ServiceMarketAdapter.OnServiceMarketClickListener() {
            @Override
            public void onBook(Service service) {
                // TODO: Xử lý đặt dịch vụ
            }
            @Override
            public void onItemClick(Service service) {
                // TODO: Xử lý xem chi tiết dịch vụ
            }
        });
        recyclerView.setAdapter(adapter);
        loadServices();
    }

    private void loadServices() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Load danh sách service từ ViewModel/Repository
        // Demo dữ liệu mẫu
        serviceList.clear();
        for (int i = 1; i <= 10; i++) {
            Service s = new Service();
            s.setTitle("Service " + i);
            s.setDescription("Description for service " + i);
            s.setPrice(100.0 + i);
            s.setAvailability("Weekends");
            serviceList.add(s);
        }
        adapter.setServiceList(serviceList);
        progressBar.setVisibility(View.GONE);
    }
} 