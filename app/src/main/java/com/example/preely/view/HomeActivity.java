package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.CategoryService;
import com.example.preely.viewmodel.ServiceMarketViewModel;
import com.example.preely.viewmodel.UnreadMessageService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final String ACTION_UPDATE_UNREAD = "UPDATE_UNREAD_COUNT";
    private static final int LIMIT_PER_PAGE = 6;
    private static final int SCROLL_THRESHOLD = 500;
    private RecyclerView cateRecycleView, serviceRecycleView;
    private ImageView searchBtn, circleImage;
    private TextView nameTv, unreadBadge, seeAllService;
    private ImageButton scrollToTopBtn, openChatButton, testMapButton;
    private ScrollView homeScrollView;
    private EditText searchInput;
    private MaterialButton favouriteButton;
    private LinearLayout mainLayout;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private CategoryService categoryService;
    private ServiceMarketViewModel serviceMarketViewModel;
    private final List<CategoryResponse> categoryList = new ArrayList<>();
    private final List<ServiceMarketResponse> serviceList = new ArrayList<>();
    private CategoryMarketAdapter categoryAdapter;
    private ServiceMarketAdapter serviceAdapter;
    private ServiceFilterRequest currentRequest;
    private BroadcastReceiver unreadCountReceiver;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isScrollListenerAttached = false;
    private boolean categoryLoaded = false;
    private boolean serviceLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        initializeComponents();
        setupViews();
        setupNotificationSystem();
        searchInputTracking();
        handleIntentExtras();
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
        setupFavouriteButton();
        setupCategoryView();
        setupServiceView();
        setupScrollFunctionality();
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

    private void setupChatButton() {
        openChatButton.setOnClickListener(v -> {
            Log.d(TAG, "Chat button clicked, isLoggedIn: " + sessionManager.getLogin());
            if (sessionManager != null && sessionManager.getLogin()) {
                updateUnreadBadge(0);
                startActivity(new Intent(HomeActivity.this, ChatListActivity.class));
            } else {
                CustomToast.makeText(this, "Vui lòng đăng nhập để chat",
                        CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
            }
        });
    }

    private void setupMapButton() {
        testMapButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MapTestActivity.class));
        });
    }

    private void setupFavouriteButton() {
        favouriteButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SavedServicesActivity.class));
        });
    }

    private void setupCategoryView() {
        categoryService = new ViewModelProvider(this).get(CategoryService.class);
        cateRecycleView.setLayoutManager(new GridLayoutManager(this, 4));
        categoryAdapter = new CategoryMarketAdapter(categoryList);
        cateRecycleView.setAdapter(categoryAdapter);
        observeCategoryList();
        categoryService.getCateList();
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

    private void setupServiceView() {
        LifecycleOwner lifecycleOwner = this;
        serviceMarketViewModel = new ViewModelProvider(this).get(ServiceMarketViewModel.class);
        serviceRecycleView.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceMarketAdapter(serviceList, lifecycleOwner, serviceMarketViewModel);
        serviceRecycleView.setAdapter(serviceAdapter);
        serviceRecycleView.setNestedScrollingEnabled(false);
        observeServiceList();
        currentRequest = new ServiceFilterRequest();
        serviceMarketViewModel.getServiceList(currentRequest);
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

    private void setupNotificationSystem() {
        if (sessionManager.getLogin()) {
            Intent serviceIntent = new Intent(this, UnreadMessageService.class);
            startService(serviceIntent);
            setupUnreadCountReceiver();
            loadInitialUnreadCount();
        } else {
            Log.w(TAG, "User not logged in, skipping notification setup");
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void setupUnreadCountReceiver() {
        unreadCountReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_UPDATE_UNREAD.equals(intent.getAction())) {
                    int unreadCount = intent.getIntExtra("unread_count", 0);
                    updateUnreadBadge(unreadCount);
                    Log.d(TAG, "Badge updated with count: " + unreadCount);
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_UNREAD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(unreadCountReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(unreadCountReceiver, filter);
        }
        Log.d(TAG, "Unread count receiver registered");
    }

    private void loadInitialUnreadCount() {
        if (!sessionManager.getLogin()) return;
        try {
            String userId = sessionManager.getUserSession().getId();
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection("user").document(userId);
            FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                    .whereEqualTo("receiver_id", userRef)
                    .whereEqualTo("is_read", false)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        int unreadCount = querySnapshot.size();
                        updateUnreadBadge(unreadCount);
                        Log.d(TAG, "Initial unread count loaded: " + unreadCount);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load initial unread count", e);
                        updateUnreadBadge(0);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadInitialUnreadCount", e);
        }
    }

    private void updateUnreadBadge(int count) {
        if (unreadBadge != null) {
            runOnUiThread(() -> {
                if (count > 0) {
                    unreadBadge.setVisibility(View.VISIBLE);
                    unreadBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                } else {
                    unreadBadge.setVisibility(View.GONE);
                }
            });
        }
    }

    private void setupScrollFunctionality() {
        scrollToTopBtn.setOnClickListener(v -> homeScrollView.smoothScrollTo(0, 0));
        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = homeScrollView.getScrollY();
            scrollToTopBtn.setVisibility(scrollY > SCROLL_THRESHOLD ? View.VISIBLE : View.GONE);
        });
    }

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

    public void getMoreData() {
        if (isLastPage) {
            Log.d(TAG, "Already at last page, skipping load more");
            return;
        }
        serviceList.add(null);
        serviceAdapter.notifyItemInserted(serviceList.size() - 1);
        serviceMarketViewModel.getServiceList(currentRequest);
    }

    private void searchInputTracking() {
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

    private void handleIntentExtras() {
        String toastMess = getIntent().getStringExtra("toast_mess");
        if (toastMess != null && !toastMess.isEmpty()) {
            CustomToast.makeText(this, toastMess, CustomToast.LENGTH_SHORT,
                    Constraints.NotificationType.SUCCESS).show();
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (unreadCountReceiver != null) {
            try {
                unregisterReceiver(unreadCountReceiver);
                Log.d(TAG, "Unread count receiver unregistered");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered: " + e.getMessage());
            }
        }
        Log.d(TAG, "HomeActivity destroyed");
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

    private void checkAllDataLoaded() {
        if (categoryLoaded && serviceLoaded) {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        }
    }
}