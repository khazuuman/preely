package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.ServiceMarketAdapter;
import com.example.preely.model.entities.Service;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.response.ServiceMarketResponse;
import com.example.preely.viewmodel.ServiceMarketViewModel;

import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends AppCompatActivity {

    ImageView backBtn, filterBtn, searchBtn, noData;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        setupView();
        initializeComponents();
        setupServiceView();
        setupServiceTracking();
    }

    public void initializeComponents() {
        currentRequest = new ServiceFilterRequest();
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
    }

    // start service display handle

    public void setupServiceView() {
        serviceRecycleView.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceMarketAdapter(serviceList);
        serviceRecycleView.setAdapter(serviceAdapter);
        observeServiceList();
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
            if (serviceResponses != null) {
                Log.i("observePostList", "In postResponses not null");

                if (serviceList.isEmpty()) {
                    serviceList.addAll(serviceResponses);
                    serviceAdapter.notifyDataSetChanged();
                } else {
                    int start = serviceList.size();
                    serviceList.addAll(serviceResponses);
                    serviceAdapter.notifyItemRangeInserted(start, serviceResponses.size());
                }
                isLoading = false;
                isLastPage = serviceResponses.size() < LIMIT_PER_PAGE;
            } else {
                Log.i("observePostList", "In postResponses is null");
            }
            if (serviceList == null || serviceList.isEmpty()) {
                homeScrollView.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
            } else {
                homeScrollView.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
            }
            serviceLoaded = true;
            checkAllDataLoaded();
        });
    }

    // end service display handle

    private void checkAllDataLoaded() {
        if (serviceLoaded) {
            progressBar.setVisibility(View.GONE);
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

} 