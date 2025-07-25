package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ServiceMarketAdapter;
import com.example.preely.model.entities.Service;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.response.ServiceMarketResponse;
import com.example.preely.viewmodel.ServiceMarketViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceListActivity extends AppCompatActivity {

    ImageView backBtn, filterBtn, searchBtn, noData, scrollToTopBtn;
    RecyclerView serviceRecycleView;
    ScrollView homeScrollView;
    EditText searchInput;
    LinearLayout mainLayout;
    ServiceMarketAdapter serviceAdapter;
    ServiceMarketViewModel serviceMarketViewModel;
    ProgressBar progressBar;
    List<ServiceMarketResponse> serviceList = new ArrayList<>();
    private static final int LIMIT_PER_PAGE = 6;
    private boolean isLoading = false, isLastPage = false;
    private ServiceFilterRequest currentRequest;
    private boolean hasLoadedOnce = false, isFiltered = false;
    private boolean serviceLoaded = false;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_list);

        setupView();
        initializeComponents();
        setupSearchInputIntent();
        setupFilterByCateIntent();
        setupServiceView();
        setupServiceTracking();
        scrollHandle();
        setupFilterBottom();
        handleFilterRequest();
        handleSearchRequest();
    }

    public void initializeComponents() {
        intent = getIntent();
        currentRequest = new ServiceFilterRequest();
        serviceMarketViewModel = new ViewModelProvider(this).get(ServiceMarketViewModel.class);
    }

    public void setupView() {
        backBtn = findViewById(R.id.back_to_home);
        filterBtn = findViewById(R.id.filterBtn);
        searchBtn = findViewById(R.id.search_btn);
        searchInput = findViewById(R.id.searchInput);
        serviceRecycleView = findViewById(R.id.service_recycle_view);
        homeScrollView = findViewById(R.id.homeScrollView);
        mainLayout = findViewById(R.id.mainLayout);
        noData = findViewById(R.id.noDataImage);
        progressBar = findViewById(R.id.progressBar);
        mainLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn);

        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    // start service display handle

    public void setupServiceView() {
        serviceRecycleView.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceMarketAdapter(serviceList);
        serviceRecycleView.setAdapter(serviceAdapter);
        observeServiceList();
        serviceMarketViewModel.getServiceList(currentRequest);
        hasLoadedOnce = true;
        addScrollListener();

    }

    public void setupServiceTracking() {
        serviceMarketViewModel.getIsLastPageResult().observe(this, value -> {
            if (value != null) {
                isLastPage = value;
                if (value) {
                    if (!serviceList.isEmpty() && serviceList.get(serviceList.size() - 1) == null) {
                        serviceList.remove(serviceList.size() - 1);
                        serviceAdapter.notifyItemRemoved(serviceList.size());
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeServiceList() {
        serviceMarketViewModel.getServiceListResult().observe(this, serviceResponses -> {
            Log.i("observePostList", "Received response");

            progressBar.setVisibility(View.GONE);
            serviceLoaded = true;
            isLoading = false;

            if (serviceResponses != null && !serviceResponses.isEmpty()) {
                int start = serviceList.size();
                serviceList.addAll(serviceResponses);

                if (start == 0) {
                    serviceAdapter.notifyDataSetChanged();
                } else {
                    serviceAdapter.notifyItemRangeInserted(start, serviceResponses.size());
                }

                homeScrollView.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
            } else {
                Log.i("observePostList", "No data received");
                progressBar.setVisibility(View.GONE);
                if (serviceList.isEmpty()) {
                    homeScrollView.setVisibility(View.GONE);
                    noData.setVisibility(View.VISIBLE);
                }
            }

            isLastPage = (serviceResponses == null || serviceResponses.size() < LIMIT_PER_PAGE);
            checkAllDataLoaded();
        });
    }


    // end service display handle

    private void checkAllDataLoaded() {
        progressBar.setVisibility(View.GONE);
        if (serviceLoaded) {
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasLoadedOnce) {
            if (!isFiltered) {
                currentRequest = new ServiceFilterRequest();
            }
            serviceMarketViewModel.getServiceList(currentRequest);
            hasLoadedOnce = true;
            addScrollListener();
        }
    }

    private final ViewTreeObserver.OnScrollChangedListener scrollChangedListener = () -> {
        View view = homeScrollView.getChildAt(homeScrollView.getChildCount() - 1);
        if (view != null) {
            int diff = view.getBottom() - (homeScrollView.getHeight() + homeScrollView.getScrollY());

            if (diff <= 0 && !isLoading && !isLastPage) {
                isLoading = true;
                getMoreData();
            }
        }
    };

    private void addScrollListener() {
        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(scrollChangedListener);
    }

    private void removeScrollListener() {
        homeScrollView.getViewTreeObserver().removeOnScrollChangedListener(scrollChangedListener);
    }

    public void getMoreData() {
        if (isLastPage) return;
        serviceList.add(null);
        serviceAdapter.notifyItemInserted(serviceList.size() - 1);
        serviceMarketViewModel.getServiceList(currentRequest);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] scrcoords = new int[2];
                v.getLocationOnScreen(scrcoords);
                float x = ev.getRawX() + v.getLeft() - scrcoords[0];
                float y = ev.getRawY() + v.getTop() - scrcoords[1];

                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    v.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void scrollHandle() {
        scrollToTopBtn.setOnClickListener(v -> {
            homeScrollView.smoothScrollTo(0, 0);
        });
        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = homeScrollView.getScrollY();
            scrollToTopBtn.setVisibility(scrollY > 500 ? View.VISIBLE : View.GONE);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setupFilterBottom() {
        FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
        filterBtn = findViewById(R.id.filterBtn);
        filterBottomSheet.setOnFilterApplyListener(filterRequest -> {
            serviceLoaded = false;
            isFiltered = true;
            hasLoadedOnce = true;
            filterRequest.setTitle(searchInput.getText().toString());
            currentRequest = filterRequest;

            mainLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            serviceMarketViewModel.resetPagination();
            serviceMarketViewModel.resetPostListResult();

            serviceList.clear();
            serviceAdapter.notifyDataSetChanged();

            removeScrollListener();
            addScrollListener();

            serviceMarketViewModel.getServiceList(currentRequest);
        });
        filterBtn.setOnClickListener(v -> {
            filterBottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void handleFilterRequest() {
        FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
        filterBtn = findViewById(R.id.filterBtn);
        filterBottomSheet.setOnFilterApplyListener(filterRequest -> {
            searchInput.setText(null);
            serviceLoaded = false;
            isFiltered = true;
            hasLoadedOnce = true;
            filterRequest.setTitle(null);
            currentRequest = filterRequest;

            mainLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            serviceMarketViewModel.resetPagination();
            serviceMarketViewModel.resetPostListResult();

            serviceList.clear();
            serviceAdapter.notifyDataSetChanged();

            removeScrollListener();
            addScrollListener();

            serviceMarketViewModel.getServiceList(currentRequest);
        });
    }

    public void handleSearchRequest() {
        searchBtn.setOnClickListener(v -> {
            performSearch();
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_NULL) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch() {
        String query = searchInput.getText().toString().trim();

        currentRequest = new ServiceFilterRequest();
        currentRequest.setTitle(query.isEmpty() ? null : query);

        currentRequest = new ServiceFilterRequest();
        currentRequest.setTitle(query);

        serviceMarketViewModel.resetPagination();
        serviceMarketViewModel.resetPostListResult();

        serviceList.clear();
        serviceAdapter.notifyDataSetChanged();

        removeScrollListener();
        addScrollListener();

        serviceMarketViewModel.getServiceList(currentRequest);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setupSearchInputIntent() {
        String searchStr = intent.getStringExtra("query");
        if (searchStr != null && !searchStr.isEmpty()) {
            searchInput.setText(searchStr);
            currentRequest.setTitle(searchStr);
        }
    }


    public void setupFilterByCateIntent() {
        String cate_id = intent.getStringExtra("category_id");
        currentRequest = new ServiceFilterRequest();
        if (cate_id != null && !cate_id.isEmpty()) {
            DocumentReference categoryRef = FirebaseFirestore.getInstance()
                    .collection("category")
                    .document(cate_id);
            List<DocumentReference> categoryList = Collections.singletonList(categoryRef);
            currentRequest.setCategory_ids(categoryList);
            isFiltered = true;
        }
    }

} 