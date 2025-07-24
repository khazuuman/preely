package com.example.preely.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ServiceAdapter;
import com.example.preely.authentication.SessionManager;
import com.example.preely.dialog.AddEditUserServiceDialog;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.User;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.ManagementServiceService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class UserServiceManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<Service> serviceList = new ArrayList<>();
    private List<Service> originalServiceList = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private ManagementServiceService managementServiceService;
    private List<Category> cachedCategories = new ArrayList<>();
    private List<User> cachedProviders = new ArrayList<>();
    private AddEditUserServiceDialog addEditServiceDialog;
    private SessionManager sessionManager;
    private String currentUserId;
    private User currentUser;
    private static final String TAG = "UserServiceManagement";
    private ActivityResultLauncher<android.content.Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_management, container, false);
        recyclerView = view.findViewById(R.id.recycler_services);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fabAdd = view.findViewById(R.id.fab_add_service);
        etSearch = view.findViewById(R.id.et_search_service);
        sessionManager = new SessionManager(requireContext());
        currentUserId = sessionManager.getUserSession() != null ? sessionManager.getUserSession().getId() : null;
        currentUser = null;
        if (sessionManager.getUserSession() != null) {
            currentUser = new User();
            currentUser.setId(sessionManager.getUserSession().getId());
            currentUser.setFull_name(sessionManager.getUserSession().getFull_name());
            currentUser.setEmail(sessionManager.getUserSession().getEmail());
        }
        managementServiceService = new ViewModelProvider(this).get(ManagementServiceService.class);
        adapter = new ServiceAdapter(serviceList, new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onEdit(Service service) {
                showAddEditServiceDialog(service, true);
            }
            @Override
            public void onDelete(Service service) {
                deleteService(service);
            }
            @Override
            public void onItemClick(Service service) {
                // TODO: Hiển thị chi tiết service nếu cần
            }
        });
        recyclerView.setAdapter(adapter);
        observeViewModel();
        fabAdd.setOnClickListener(v -> showAddEditServiceDialog(null, false));
        setupSearch();
        managementServiceService.loadServices();
        managementServiceService.fetchCategories();
        managementServiceService.fetchUsers();
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (addEditServiceDialog != null && result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    addEditServiceDialog.onImagesPicked(result.getData());
                }
            }
        );
        return view;
    }

    private void observeViewModel() {
        managementServiceService.getServiceList().observe(getViewLifecycleOwner(), services -> {
            serviceList.clear();
            originalServiceList.clear();
            if (services != null) {
                for (Service s : services) {
                    if (s.getProvider_id() != null && currentUserId != null && s.getProvider_id().getId().equals(currentUserId)) {
                        serviceList.add(s);
                        originalServiceList.add(s);
                    }
                }
            }
            adapter.setServiceList(serviceList);
        });
        managementServiceService.getCategoryList().observe(getViewLifecycleOwner(), categories -> {
            cachedCategories.clear();
            if (categories != null) cachedCategories.addAll(categories);
            Map<String, String> categoryIdToName = new HashMap<>();
            for (Category c : cachedCategories) categoryIdToName.put(c.getId(), c.getName());
            adapter.setCategoryIdToName(categoryIdToName);
        });
        managementServiceService.getUserList().observe(getViewLifecycleOwner(), users -> {
            cachedProviders.clear();
            if (users != null) cachedProviders.addAll(users);
            Map<String, String> providerIdToName = new HashMap<>();
            for (User u : cachedProviders) providerIdToName.put(u.getId(), u.getFull_name());
            adapter.setProviderIdToName(providerIdToName);
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServices(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterServices(String query) {
        if (query.isEmpty()) {
            serviceList.clear();
            serviceList.addAll(originalServiceList);
        } else {
            List<Service> filtered = new ArrayList<>();
            for (Service s : originalServiceList) {
                if ((s.getTitle() != null && s.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                    (s.getDescription() != null && s.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(s);
                }
            }
            serviceList.clear();
            serviceList.addAll(filtered);
        }
        adapter.setServiceList(serviceList);
    }

    private void showAddEditServiceDialog(Service service, boolean isEdit) {
        if (cachedCategories.isEmpty() || cachedProviders.isEmpty()) {
            Toast.makeText(getContext(), "Loading data...", Toast.LENGTH_SHORT).show();
            managementServiceService.fetchCategories();
            managementServiceService.fetchUsers();
            return;
        }
        List<String> availabilityLabels = new ArrayList<>();
        for (Constraints.Availability a : Constraints.Availability.values()) {
            availabilityLabels.add(a.getLabel());
        }
        addEditServiceDialog = new AddEditUserServiceDialog(
            getContext(),
            service,
            cachedCategories,
            cachedProviders,
            availabilityLabels,
            (savedService, editMode) -> {
                // Luôn gán provider_id là user hiện tại
                if (currentUserId != null) {
                    for (User u : cachedProviders) {
                        if (u.getId().equals(currentUserId)) {
                            savedService.setProvider_id(com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("user").document(currentUserId));
                            break;
                        }
                    }
                }
                if (editMode) {
                    managementServiceService.updateService(savedService);
                } else {
                    managementServiceService.addService(savedService);
                }
            },
            imagePickerLauncher,
            currentUser
        );
        addEditServiceDialog.show();
    }

    private void deleteService(Service service) {
        managementServiceService.deleteService(service);
    }
} 