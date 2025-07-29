package com.example.preely.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ServiceAdapter;
import com.example.preely.model.entities.Service;
import com.example.preely.dialog.AddEditServiceDialog;
import com.example.preely.util.Constraints;
import com.example.preely.view.FullscreenMapPickerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.User;
import androidx.lifecycle.ViewModelProvider;
import com.example.preely.viewmodel.ManagementServiceService;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.HashMap;
import java.util.Map;

public class ServiceManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<Service> serviceList = new ArrayList<>();
    private List<Service> originalServiceList = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private ManagementServiceService managementServiceService;
    private List<Category> cachedCategories = new ArrayList<>();
    private List<User> cachedProviders = new ArrayList<>();

    //  Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> mapPickerLauncher; // Thêm map picker launcher

    private AddEditServiceDialog addEditServiceDialog;
    private static final String TAG = "ServiceManagement";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_management, container, false);

        initViews(view);
        setupRecyclerView();
        setupViewModels();
        setupActivityResultLaunchers(); //  Setup launchers
        setupListeners();
        setupSearch();

        // Load initial data
        managementServiceService.loadServices();
        managementServiceService.fetchCategories();
        managementServiceService.fetchUsers();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_services);
        fabAdd = view.findViewById(R.id.fab_add_service);
        etSearch = view.findViewById(R.id.et_search_service);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                showServiceDetailDialog(service);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModels() {
        managementServiceService = new ViewModelProvider(this).get(ManagementServiceService.class);
        observeViewModel();
    }

    /**
     *  Setup Activity Result Launchers (image + map)
     */
    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (addEditServiceDialog != null && result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        addEditServiceDialog.onImagesPicked(result.getData());
                    }
                }
        );

        //  Map picker launcher (thêm mới)
        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        GeoPoint newLocation = FullscreenMapPickerActivity.getSelectedLocation(result.getData());
                        if (newLocation != null && addEditServiceDialog != null) {
                            addEditServiceDialog.handleMapPickerResult(newLocation);
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddEditServiceDialog(null, false));
    }

    private void observeViewModel() {
        managementServiceService.getServiceList().observe(getViewLifecycleOwner(), services -> {
            Log.d(TAG, "LiveData getServiceList changed, size: " + (services != null ? services.size() : 0));
            serviceList.clear();
            originalServiceList.clear();
            if (services != null) {
                serviceList.addAll(services);
                originalServiceList.addAll(services);
            }
            adapter.setServiceList(serviceList);
            Log.d(TAG, "Adapter setServiceList, size: " + serviceList.size());
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
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServices(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterServices(String query) {
        Log.d(TAG, "Filter query: " + query);
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
        Log.d(TAG, "Adapter setServiceList after filter, size: " + serviceList.size());
    }

    /**
     *  Show dialog với map picker launcher
     */
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

        //  Pass both launchers to dialog
        addEditServiceDialog = new AddEditServiceDialog(
                getContext(),
                service,
                cachedCategories,
                cachedProviders,
                availabilityLabels,
                (savedService, editMode) -> {
                    if (editMode) {
                        managementServiceService.updateService(savedService);
                    } else {
                        managementServiceService.addService(savedService);
                    }
                },
                imagePickerLauncher,
                mapPickerLauncher //  Pass map picker launcher
        );

        addEditServiceDialog.show();

        //  Clear reference when dismissed
        addEditServiceDialog.setOnDismissListener(dialog -> addEditServiceDialog = null);
    }

    private void deleteService(Service service) {
        managementServiceService.deleteService(service);
    }

    private void showServiceDetailDialog(Service service) {
        // TODO: Hiển thị dialog chi tiết service (tùy ý)
    }
}
