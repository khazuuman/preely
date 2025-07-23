package com.example.preely.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ServiceAdapter;
import com.example.preely.model.entities.Service;
import java.util.ArrayList;
import java.util.List;

public class MyServicesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ServiceAdapter adapter;
    private List<Service> myServiceList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_services);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        adapter = new ServiceAdapter(myServiceList, new ServiceAdapter.OnServiceClickListener() {
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
        loadMyServices();
    }

    private void loadMyServices() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Load danh sách service của tôi từ ViewModel/Repository
        // Demo dữ liệu mẫu
        myServiceList.clear();
        for (int i = 1; i <= 5; i++) {
            Service s = new Service();
            s.setTitle("My Service " + i);
            s.setDescription("Description for my service " + i);
            s.setPrice(200.0 + i);
            // TODO: set provider_id, category_id nếu cần
            myServiceList.add(s);
        }
        adapter.setServiceList(myServiceList);
        progressBar.setVisibility(View.GONE);
    }
} 