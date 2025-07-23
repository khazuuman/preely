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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.PostMarketAdapter;
import com.example.preely.model.request.PostFilterRequest;
import com.example.preely.model.response.PostResponse;
import com.example.preely.util.ViewUtil;
import com.example.preely.viewmodel.PostService;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PostListActivity extends AppCompatActivity {

    ImageView filterBtn, searchIcon, noData;
    ImageButton scrollToTopBtn;
    EditText searchInput;
    private static final int LIMIT_PER_PAGE = 6;
    private boolean isLoading = false, isLastPage = false;
    private PostFilterRequest currentRequest;
    ScrollView homeScrollView;
    RecyclerView postRecycleView;
    ProgressBar progressBar;
    LinearLayout mainLayout;
    private PostMarketAdapter postAdapter;
    private PostService postService;
    private final List<PostResponse> postList = new ArrayList<>();
    private boolean hasLoadedOnce = false, isFiltered = false;
    private boolean postLoaded = false;

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_list);

        noData = findViewById(R.id.noDataImage);
        searchIcon = findViewById(R.id.search_icon);
        progressBar = findViewById(R.id.progressBar);
        mainLayout = findViewById(R.id.mainLayout);
        homeScrollView = findViewById(R.id.homeScrollView);
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn);

        mainLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        postService = new ViewModelProvider(this).get(PostService.class);

        Intent intent = getIntent();

        // handle cate_id from intent
        String cate_id = intent.getStringExtra("category_id");
        currentRequest = new PostFilterRequest();
        if (cate_id != null && !cate_id.isEmpty()) {
            DocumentReference categoryRef = FirebaseFirestore.getInstance()
                    .collection("category")
                    .document(cate_id);
            List<DocumentReference> categoryList = Collections.singletonList(categoryRef);
            currentRequest.setCategory_id(categoryList);
            isFiltered = true;
        }

        // handle search input from intent
        searchInput = findViewById(R.id.searchInput);
        String searchStr = intent.getStringExtra("query");
        if (searchStr != null && !searchStr.isEmpty()) {
            searchInput.setText(searchStr);
            currentRequest.setTitle(searchStr);
            isFiltered = true;
        }

        postService.getPostList(currentRequest);
        hasLoadedOnce = true;
        addScrollListener();

        FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
        filterBtn = findViewById(R.id.filterBtn);
        filterBottomSheet.setOnFilterApplyListener(filterRequest -> {
            postLoaded = false;
            isFiltered = true;
            hasLoadedOnce = true;
            filterRequest.setTitle(searchInput.getText().toString());
            currentRequest = filterRequest;

            mainLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            postService.resetPagination();
            postService.resetPostListResult();

            postList.clear();
            postAdapter.notifyDataSetChanged();

            removeScrollListener();
            addScrollListener();

            postService.getPostList(currentRequest);
        });
        filterBtn.setOnClickListener(v -> {
            filterBottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });

        scrollToTopBtn.setOnClickListener(v -> {
            homeScrollView.smoothScrollTo(0, 0);
        });
        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = homeScrollView.getScrollY();
            scrollToTopBtn.setVisibility(scrollY > 500 ? View.VISIBLE : View.GONE);
        });

        postRecycleView = findViewById(R.id.post_recycle_view);
        postRecycleView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostMarketAdapter(postList);
        postRecycleView.setAdapter(postAdapter);
        observePostList();
        currentRequest = new PostFilterRequest();
        // input search
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_NULL) {
                performSearch();
                return true;
            }
            return false;
        });


        postService.getIsLastPageResult().observe(this, value -> {
            if (value != null) {
                isLastPage = value;
                if (value) {
                    if (!postList.isEmpty() && postList.get(postList.size() - 1) == null) {
                        postList.remove(postList.size() - 1);
                        postAdapter.notifyItemRemoved(postList.size());
                    }
                }
            }
        });
        postService.getSavedPostsStatus().observe(this, map -> {
            if (map != null) {
                postAdapter.setSavedPostsStatusMap(map);
                postAdapter.notifyDataSetChanged();
            }
        });
        searchIcon.setOnClickListener(v -> {
            performSearch();
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(PostListActivity.this, HomeActivity.class));
            finish();
        });
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) return;

        if (currentRequest == null) {
            currentRequest = new PostFilterRequest();
        }
        currentRequest.setTitle(query);

        postService.resetPagination();
        postService.resetPostListResult();

        postList.clear();
        postAdapter.notifyDataSetChanged();

        removeScrollListener();
        addScrollListener();

        postService.getPostList(currentRequest);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!hasLoadedOnce) {
            if (!isFiltered) {
                currentRequest = new PostFilterRequest();
            }
            postService.getPostList(currentRequest);
            hasLoadedOnce = true;
            addScrollListener();
        }
    }

    public void getMoreData() {
        if (isLastPage) return;
        postList.add(null);
        postAdapter.notifyItemInserted(postList.size() - 1);
        postService.getPostList(currentRequest);
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

    @SuppressLint("NotifyDataSetChanged")
    private void observePostList() {
        postService.getPostListResult().observe(this, postResponses -> {
            if (postResponses != null) {
                Log.i("observePostList", "In postResponses not null");

                if (postList.isEmpty()) {
                    postList.addAll(postResponses);
                    postAdapter.notifyDataSetChanged();
                } else {
                    int start = postList.size();
                    postList.addAll(postResponses);
                    postAdapter.notifyItemRangeInserted(start, postResponses.size());
                }
                isLoading = false;
                isLastPage = postResponses.size() < LIMIT_PER_PAGE;
            } else {
                Log.i("observePostList", "In postResponses is null");
            }
            if (postList == null || postList.isEmpty()) {
                homeScrollView.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
            } else {
                homeScrollView.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
            }
            postLoaded = true;
            checkAllDataLoaded();
        });
    }

    private void checkAllDataLoaded() {
        if (postLoaded) {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
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
                    // Click ngoài EditText detected
                    // 1. Clear focus để bỏ focus khỏi EditText
                    v.clearFocus();

                    // 2. Ẩn bàn phím
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