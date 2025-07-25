package com.example.preely.view;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.preely.R;
import com.example.preely.adapter.SavedServiceAdapter;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.SavedService;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.response.UserResponse;
import com.example.preely.util.Constraints;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.User;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;

public class SavedServicesActivity extends AppCompatActivity implements SavedServiceAdapter.OnSavedServiceClickListener {
    private RecyclerView recyclerView;
    private SavedServiceAdapter adapter;
    private List<Service> savedServices = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private SwipeRefreshLayout swipeRefresh;
    private SessionManager sessionManager;
    private UserResponse currentUser;
    private Map<String, String> providerIdToName = new HashMap<>();
    private Map<String, String> categoryIdToName = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_services);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.saved_services);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new SavedServiceAdapter(savedServices, this);
        recyclerView.setAdapter(adapter);
        sessionManager = new SessionManager(this);
        currentUser = sessionManager.getUserSession();
        swipeRefresh.setOnRefreshListener(this::loadSavedServices);
        loadSavedServices();
    }

    private void loadSavedServices() {
        progressBar.setVisibility(View.VISIBLE);
        savedServices.clear();
        adapter.notifyDataSetChanged();
        if (currentUser == null || currentUser.getId() == null) {
            showEmptyState();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection(Constraints.CollectionName.SAVED_SERVICE)
                .whereEqualTo("user_id", db.collection(Constraints.CollectionName.USERS).document(currentUser.getId()));
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<SavedService> savedServiceRefs = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    SavedService ss = doc.toObject(SavedService.class);
                    savedServiceRefs.add(ss);
                }
                if (savedServiceRefs.isEmpty()) {
                    showEmptyState();
                    return;
                }
                // Lấy chi tiết từng service
                fetchServiceDetails(savedServiceRefs);
            } else {
                showEmptyState();
            }
        });
    }

    private void fetchServiceDetails(List<SavedService> savedServiceRefs) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        savedServices.clear();
        providerIdToName.clear();
        categoryIdToName.clear();
        if (savedServiceRefs.isEmpty()) {
            showEmptyState();
            return;
        }
        final int[] loaded = {0};
        for (SavedService ss : savedServiceRefs) {
            DocumentReference serviceRef = ss.getService_id();
            if (serviceRef != null) {
                serviceRef.get().addOnSuccessListener(documentSnapshot -> {
                    Service service = documentSnapshot.toObject(Service.class);
                    if (service != null) {
                        savedServices.add(service);
                        // Lấy tên provider
                        if (service.getProvider_id() != null) {
                            String providerId = service.getProvider_id().getId();
                            if (!providerIdToName.containsKey(providerId)) {
                                service.getProvider_id().get().addOnSuccessListener(providerSnap -> {
                                    User user = providerSnap.toObject(User.class);
                                    if (user != null) {
                                        providerIdToName.put(providerId, user.getFull_name());
                                        adapter.setProviderIdToName(providerIdToName);
                                    }
                                });
                            }
                        }
                        // Lấy tên category
                        if (service.getCategory_id() != null) {
                            String categoryId = service.getCategory_id().getId();
                            if (!categoryIdToName.containsKey(categoryId)) {
                                service.getCategory_id().get().addOnSuccessListener(categorySnap -> {
                                    Category category = categorySnap.toObject(Category.class);
                                    if (category != null) {
                                        categoryIdToName.put(categoryId, category.getName());
                                        adapter.setCategoryIdToName(categoryIdToName);
                                    }
                                });
                            }
                        }
                    }
                    loaded[0]++;
                    if (loaded[0] == savedServiceRefs.size()) {
                        updateUIAfterLoad();
                    }
                }).addOnFailureListener(e -> {
                    loaded[0]++;
                    if (loaded[0] == savedServiceRefs.size()) {
                        updateUIAfterLoad();
                    }
                });
            } else {
                loaded[0]++;
                if (loaded[0] == savedServiceRefs.size()) {
                    updateUIAfterLoad();
                }
            }
        }
    }

    private void updateUIAfterLoad() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        adapter.notifyDataSetChanged();
        if (savedServices.isEmpty()) {
            showEmptyState();
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRemove(Service service) {
        // TODO: Xử lý xóa khỏi danh sách saved services (và xóa trên Firestore nếu cần)
        savedServices.remove(service);
        adapter.notifyDataSetChanged();
        if (savedServices.isEmpty()) {
            showEmptyState();
        }
    }

    @Override
    public void onItemClick(Service service) {
        // Mở ServiceDetailActivity với id của service
        Intent intent = new Intent(this, ServiceDetailActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }
} 