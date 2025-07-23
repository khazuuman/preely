package com.example.preely.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.SavedServiceAdapter;
import com.example.preely.model.entities.Service;
import java.util.ArrayList;
import java.util.List;

public class SavedServicesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SavedServiceAdapter adapter;
    private List<Service> savedServiceList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_services);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        adapter = new SavedServiceAdapter(savedServiceList, new SavedServiceAdapter.OnSavedServiceClickListener() {
            @Override
            public void onRemove(Service service) {
                // TODO: Xử lý xóa khỏi danh sách đã lưu
            }
            @Override
            public void onItemClick(Service service) {
                // TODO: Xử lý xem chi tiết dịch vụ
            }
        });
        recyclerView.setAdapter(adapter);
        loadSavedServices();
    }

    private void loadSavedServices() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Load danh sách dịch vụ đã lưu từ ViewModel/Repository
        // Demo dữ liệu mẫu
        savedServiceList.clear();
        for (int i = 1; i <= 3; i++) {
            Service s = new Service();
            s.setTitle("Saved Service " + i);
            s.setDescription("Description for saved service " + i);
            s.setPrice(150.0 + i);
            savedServiceList.add(s);
        }
        adapter.setServiceList(savedServiceList);
        progressBar.setVisibility(View.GONE);
    }
} 