package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.example.preely.R;
import com.example.preely.adapter.CategoryMarketAdapter;
import com.example.preely.adapter.ServiceMarketAdapter;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.model.response.ServiceResponse;
import com.example.preely.model.response.UserResponse;
import com.example.preely.viewmodel.UnreadMessageService;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.CategoryService;
import com.example.preely.viewmodel.ServiceViewModel;
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
    private TextView nameTv, unreadBadge;
    private ImageButton scrollToTopBtn, openChatButton, testMapButton;
    private ScrollView homeScrollView;
    private final List<CategoryResponse> categoryList = new ArrayList<>();
    private final List<Service> serviceList = new ArrayList<>();
    private CategoryMarketAdapter categoryAdapter;
    private ServiceMarketAdapter serviceAdapter;
    private CategoryService categoryService;
    TextView seeAllService;
    EditText searchInput;
    private ServiceViewModel serviceViewModel;
    private SessionManager sessionManager;
    LinearLayout mainLayout;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isScrollListenerAttached = false;
    private ServiceFilterRequest currentServiceRequest;
    private BroadcastReceiver unreadCountReceiver;
    private ProgressBar progressBar;
    private boolean categoryLoaded = false;
    private boolean postLoaded = false;
    private MaterialButton favouriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        initializeComponents();
        setupViews();
        setupNotificationSystem();
        setupDataServices();
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
        setupScrollFunctionality();
        setupFavouriteButton();
    }

    private void findViews() {
        cateRecycleView = findViewById(R.id.cate_recycle_view);
        serviceRecycleView = findViewById(R.id.service_recycle_view);
        nameTv = findViewById(R.id.nameTv);
        unreadBadge = findViewById(R.id.unread_badge);
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn);
        mainLayout = findViewById(R.id.mainLayout);
        progressBar = findViewById(R.id.progressBar);
        mainLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        nameTv = findViewById(R.id.nameTv);
        homeScrollView = findViewById(R.id.homeScrollView);
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn);
        seeAllService = findViewById(R.id.seeAllService);
        UserResponse user = sessionManager.getUserSession();
        if (user != null) {
            nameTv.setText(user.getFull_name() == null ? user.getUsername() : user.getFull_name());
        }
        openChatButton = findViewById(R.id.button_open_chat);
        testMapButton = findViewById(R.id.test_map_button);
        homeScrollView = findViewById(R.id.homeScrollView);
        favouriteButton = findViewById(R.id.button_favourite);
    }

    private void setupUserInfo() {
        if (sessionManager.getUserSession() != null) {
            String displayName = sessionManager.getUserSession().getFull_name();
            if (displayName == null || displayName.isEmpty()) {
                displayName = sessionManager.getUserSession().getUsername();
            }
            nameTv.setText(displayName != null ? displayName : "User");
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

    private void setupMapButton(){
        testMapButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MapTestActivity.class));
        });
    }

    private void setupScrollFunctionality() {
        scrollToTopBtn.setOnClickListener(v -> homeScrollView.smoothScrollTo(0, 0));

        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = homeScrollView.getScrollY();
            scrollToTopBtn.setVisibility(scrollY > SCROLL_THRESHOLD ? View.VISIBLE : View.GONE);
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
            String userId = sessionManager.getUserSession().getId().getId();
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
                        updateUnreadBadge(0); // Fallback to 0
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

    private void setupDataServices() {
        setupCategoryService();
        setupServiceViewModel();
    }

    private void setupCategoryService() {
        categoryService = new ViewModelProvider(this).get(CategoryService.class);

        cateRecycleView.setLayoutManager(new GridLayoutManager(this, 4));
        categoryAdapter = new CategoryMarketAdapter(categoryList);
        cateRecycleView.setAdapter(categoryAdapter);

        observeCategoryList();

        LifecycleOwner lifecycleOwner = this;
        categoryService.getCateList();
    }

    private void setupServiceViewModel() {
        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        // ... observer logic cho service nếu cần ...
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

//    private void attachScrollListener() {
//        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
//            View view = homeScrollView.getChildAt(homeScrollView.getChildCount() - 1);
//            int diff = view.getBottom() - (homeScrollView.getHeight() + homeScrollView.getScrollY());
//
//            if (diff <= 0 && !isLoading && !isLastPage) {
//                isLoading = true;
//                getMoreData();
//            }
//        });
//    }

    @SuppressLint("NotifyDataSetChanged")
    private void observePostList() {
        serviceViewModel.getServiceList().observe(this, serviceResponses -> {
            if (serviceResponses != null) {
                serviceList.addAll(serviceResponses);
                // ... logic ...
            }
        });
    }

    public void getMoreData() {
        if (isLastPage) {
            Log.d(TAG, "Already at last page, skipping load more");
            return;
        }

        Log.d(TAG, "Loading more posts...");
    }

    private void handleIntentExtras() {
        String toastMess = getIntent().getStringExtra("toast_mess");
        if (toastMess != null && !toastMess.isEmpty()) {
            CustomToast.makeText(this, toastMess, CustomToast.LENGTH_SHORT,
                    Constraints.NotificationType.SUCCESS).show();
        }
    }

    private void setupFavouriteButton() {
        favouriteButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SavedServicesActivity.class));
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

    private void checkAllDataLoaded() {
        if (categoryLoaded && postLoaded) {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

}
