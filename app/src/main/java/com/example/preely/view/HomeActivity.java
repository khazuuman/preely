package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.CategoryAdapter;
import com.example.preely.adapter.PostAdapter;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.PostFilterRequest;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.model.response.PostResponse;
import com.example.preely.util.Constraints;
import com.example.preely.viewmodel.CategoryService;
import com.example.preely.viewmodel.PostService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private final List<CategoryResponse> categoryList = new ArrayList<>();
    private final List<PostResponse> postList = new ArrayList<>();
    private CategoryService categoryService;
    private PostService postService;
    private CategoryAdapter categoryAdapter;
    private PostAdapter postAdapter;
    RecyclerView cateRecycleView, postRecycleView;
    TextView nameTv;
    ImageButton scrollToTopBtn;
    ScrollView homeScrollView;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isScrollListenerAttached = false;
    private PostFilterRequest currentRequest;
    private static final int LIMIT_PER_PAGE = 6;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        SessionManager sessionManager = new SessionManager(this);

        nameTv = findViewById(R.id.nameTv);
        homeScrollView = findViewById(R.id.homeScrollView);
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn);
        nameTv.setText(sessionManager.getUserSession().getFull_name());
        scrollToTopBtn.setOnClickListener(v -> {
            homeScrollView.smoothScrollTo(0, 0);
        });
        homeScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = homeScrollView.getScrollY();
            scrollToTopBtn.setVisibility(scrollY > 500 ? View.VISIBLE : View.GONE);
        });

        String toastMess = getIntent().getStringExtra("toast_mess");
        if (toastMess != null) {
            CustomToast.makeText(this, toastMess, CustomToast.LENGTH_SHORT, Constraints.NotificationType.SUCCESS).show();
        }

        // category list
        categoryService = new ViewModelProvider(this).get(CategoryService.class);
        observeCategoryList();
        categoryService.getCateList();
        cateRecycleView = findViewById(R.id.cate_recycle_view);
        cateRecycleView.setLayoutManager(new GridLayoutManager(this, 4));
        categoryAdapter = new CategoryAdapter(categoryList);
        cateRecycleView.setAdapter(categoryAdapter);

        // post list
        postService = new ViewModelProvider(this).get(PostService.class);
        postRecycleView = findViewById(R.id.post_recycle_view);
        postRecycleView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postList);
        postRecycleView.setAdapter(postAdapter);
        observePostList();
        currentRequest = new PostFilterRequest();
        postService.getPostList(currentRequest);

        postService.getIsLastPageResult().observe(this, value -> {
            if (value != null) {
                isLastPage = value;
            }
        });
    }

    public void getMoreData() {
        if (isLastPage) return;
        postList.add(null);
        postAdapter.notifyItemInserted(postList.size() - 1);
        postService.getPostList(currentRequest);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeCategoryList() {
        categoryService.getCateListResult().observe(this, categoryResponses -> {
            if (categoryResponses != null) {
                categoryList.clear();
                categoryList.addAll(categoryResponses);
                categoryAdapter.notifyDataSetChanged();
            }
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

                    if (!isScrollListenerAttached) {
                        attachScrollListener();
                        isScrollListenerAttached = true;
                    }
                } else {
                    int start = postList.size();
                    postList.addAll(postResponses);
                    postAdapter.notifyItemRangeInserted(start, postResponses.size());
                }
                isLoading = false;
                if (postResponses.size() < LIMIT_PER_PAGE) {
                    isLastPage = true;
                }
            }
        });
    }
}