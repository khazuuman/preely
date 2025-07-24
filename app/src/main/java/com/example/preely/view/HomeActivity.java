package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.preely.R;
import com.example.preely.adapter.CategoryMarketAdapter;
import com.example.preely.adapter.ServiceMarketAdapter;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.model.response.ServiceMarketResponse;
import com.example.preely.model.response.UserResponse;
import com.example.preely.viewmodel.UnreadMessageService;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.CategoryService;
import com.example.preely.viewmodel.ServiceMarketViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.preely.model.entities.Service;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final String ACTION_UPDATE_UNREAD = "UPDATE_UNREAD_COUNT";
    private static final int LIMIT_PER_PAGE = 6;
    private static final int SCROLL_THRESHOLD = 500;
    private RecyclerView cateRecycleView, serviceRecycleView;
    ImageView searchBtn;
    private TextView nameTv, unreadBadge;
    private ImageButton scrollToTopBtn, openChatButton, testMapButton;
    private ScrollView homeScrollView;
    private final List<CategoryResponse> categoryList = new ArrayList<>();
    private final List<ServiceMarketResponse> serviceList = new ArrayList<>();
    private CategoryMarketAdapter categoryAdapter;
    private ServiceMarketAdapter serviceAdapter;
    private CategoryService categoryService;
    TextView seeAllService;
    EditText searchInput;
    ImageView circleImage;
    private ServiceMarketViewModel serviceMarketViewModel;
    private SessionManager sessionManager;
    LinearLayout mainLayout;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isScrollListenerAttached = false;
    private BroadcastReceiver unreadCountReceiver;
    private ProgressBar progressBar;
    private boolean categoryLoaded = false;
    private boolean serviceLoaded = false;
    private ServiceFilterRequest currentRequest;
    private MaterialButton favouriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        initializeComponents();
        setupViews();
        searchInputTracking();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        Log.d(TAG, "getLogin onCreate: " + sessionManager.getLogin());
    }

    private void setupViews() {
        findViews();
        setupUserInfo();
        setupChatButton();
        setupMapButton();
        setupCategoryView();
        setupServiceView();
        scrollHandle();
    }

    private void findViews() {
        cateRecycleView = findViewById(R.id.cate_recycle_view);
        serviceRecycleView = findViewById(R.id.service_recycle_view);
        nameTv = findViewById(R.id.nameTv);
        unreadBadge = findViewById(R.id.unread_badge);
        mainLayout = findViewById(R.id.mainLayout);
        progressBar = findViewById(R.id.progressBar);
        mainLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        homeScrollView = findViewById(R.id.homeScrollView);
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn);
        seeAllService = findViewById(R.id.seeAllService);
        circleImage = findViewById(R.id.circleImageView);
        openChatButton = findViewById(R.id.button_open_chat);
        testMapButton = findViewById(R.id.test_map_button);
        favouriteButton = findViewById(R.id.button_favourite);
        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);
    }

    private void setupUserInfo() {
        UserResponse user = sessionManager.getUserSession();
        if (user != null) {
            nameTv.setText(user.getFull_name() == null ? user.getUsername() : user.getFull_name());
            if (user.getAvatar() == null) {
                circleImage.setImageResource(R.drawable.img_avatar);
            } else {
                Glide.with(this)
                        .load(user.getAvatar())
                        .circleCrop()
                        .placeholder(R.drawable.img_avatar)
                        .into(circleImage);
            }
        }
    }

    // start category display handle
    public void setupCategoryView() {
        categoryService = new ViewModelProvider(this).get(CategoryService.class);
        observeCategoryList();
        categoryService.getCateList();
        cateRecycleView = findViewById(R.id.cate_recycle_view);
        cateRecycleView.setLayoutManager(new GridLayoutManager(this, 4));
        categoryAdapter = new CategoryMarketAdapter(categoryList);
        cateRecycleView.setAdapter(categoryAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeCategoryList() {
        categoryService.getCateListResult().observe(this, categoryResponses -> {
            if (categoryResponses != null) {
                categoryList.clear();
                categoryList.addAll(categoryResponses);
                categoryAdapter.notifyDataSetChanged();
                categoryLoaded = true;
                checkAllDataLoaded();
            }
        });
    }

    // end category display handle

    // start service display handle

    public void setupServiceView() {
        LifecycleOwner lifecycleOwner = this;
        serviceMarketViewModel = new ViewModelProvider(this).get(ServiceMarketViewModel.class);
        serviceRecycleView.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceMarketAdapter(serviceList, lifecycleOwner, serviceMarketViewModel);
        serviceRecycleView.setAdapter(serviceAdapter);
        observeServiceList();
        currentRequest = new ServiceFilterRequest();
        serviceMarketViewModel.getServiceList(currentRequest);
        serviceRecycleView.setNestedScrollingEnabled(false);
//        serviceMarketViewModel.getSavedPostsStatus().observe(this, map -> {
//            if (map != null) {
//                postAdapter.setSavedPostsStatusMap(map);
//                postAdapter.notifyDataSetChanged();
//            }
//        });
        serviceMarketViewModel.getIsLastPageResult().observe(this, value -> {
            if (value != null) {
                isLastPage = value;
            }
        });
        seeAllService.setOnClickListener(v -> {
            startActivity(new Intent(this, ServiceListActivity.class));
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeServiceList() {
        serviceMarketViewModel.getServiceListResult().observe(this, serviceResponse -> {
            if (serviceResponse != null) {
                if (!serviceList.isEmpty() && serviceList.get(serviceList.size() - 1) == null) {
                    serviceList.remove(serviceList.size() - 1);
                    serviceAdapter.notifyItemRemoved(serviceList.size());
                }
                if (serviceList.isEmpty()) {
                    serviceList.addAll(serviceResponse);
                    serviceAdapter.notifyDataSetChanged();

                    if (!isScrollListenerAttached) {
                        attachScrollListener();
                        isScrollListenerAttached = true;
                    }
                } else {
                    int start = serviceList.size();
                    serviceList.addAll(serviceResponse);
                    serviceAdapter.notifyItemRangeInserted(start, serviceResponse.size());
                }
                isLoading = false;
                if (serviceResponse.size() < LIMIT_PER_PAGE) {
                    isLastPage = true;
                }
                serviceLoaded = true;
                checkAllDataLoaded();
            }
        });
    }

    public void getMoreData() {
        if (isLastPage) return;
        serviceList.add(null);
        serviceAdapter.notifyItemInserted(serviceList.size() - 1);
        serviceMarketViewModel.getServiceList(currentRequest);
    }

    // end service display handle


    private void attachScrollListener() {
        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View view = homeScrollView.getChildAt(homeScrollView.getChildCount() - 1);
            int diff = view.getBottom() - (homeScrollView.getHeight() + homeScrollView.getScrollY());

            if (diff <= 0 && !isLoading && !isLastPage) {
                isLoading = true;
                getMoreData();
            }
        });
    }

    private void checkAllDataLoaded() {
        if (categoryLoaded && serviceLoaded) {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        }
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

    public void searchInputTracking() {
        searchBtn.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                Intent intent = new Intent(this, ServiceListActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
            }
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_NULL) {
                String query = searchInput.getText().toString().trim();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                }

                Intent intent = new Intent(this, ServiceListActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);

                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionManager != null && !sessionManager.getLogin()) {
            Log.d(TAG, "Session expired, redirecting to login");
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        if (sessionManager.getLogin()) {
            loadInitialUnreadCount();
        }
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
