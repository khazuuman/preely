package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.example.preely.viewmodel.PostService;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PostListActivity extends AppCompatActivity {

    ImageView filterBtn;
    ImageButton scrollToTopBtn;
    EditText searchInput;
    private static final int LIMIT_PER_PAGE = 6;
    private boolean isLoading = false, isLastPage = false;
    private PostFilterRequest currentRequest;
    ScrollView homeScrollView;
    RecyclerView postRecycleView;
    private PostMarketAdapter postAdapter;
    private PostService postService;
    private final List<PostResponse> postList = new ArrayList<>();
    private boolean hasLoadedOnce = false, isFiltered = false;

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_list);

        homeScrollView = findViewById(R.id.homeScrollView);
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn);

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
            isFiltered = true;
            hasLoadedOnce = true;
            filterRequest.setTitle(searchInput.getText().toString());
            currentRequest = filterRequest;

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

                isFiltered = true;
                hasLoadedOnce = true;

                String query = searchInput.getText().toString().trim();

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

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                }

                postService.getPostList(currentRequest);

                return true;
            }
            return false;
        });


        postService.getIsLastPageResult().observe(this, value -> {
            if (value != null) {
                isLastPage = value;
            }
        });
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
                if (!postList.isEmpty() && postList.get(postList.size() - 1) == null) {
                    postList.remove(postList.size() - 1);
                    postAdapter.notifyItemRemoved(postList.size());
                }

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
            }
        });
    }
}