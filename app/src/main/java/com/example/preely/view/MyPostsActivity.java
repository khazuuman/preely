package com.example.preely.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.preely.R;
import com.example.preely.adapter.PostMarketAdapter;
import com.example.preely.model.response.PostResponse;
import com.example.preely.viewmodel.PostService;
import com.example.preely.authentication.SessionManager;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyPostsActivity extends AppCompatActivity {
    private RecyclerView myPostsRecyclerView;
    private PostMarketAdapter postAdapter;
    private List<PostResponse> myPostList = new ArrayList<>();
    private PostService postService;
    private Button addPostBtn;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        myPostsRecyclerView = findViewById(R.id.my_posts_recycler_view);
        addPostBtn = findViewById(R.id.add_post_btn);
        postAdapter = new PostMarketAdapter(myPostList);
        myPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myPostsRecyclerView.setAdapter(postAdapter);
        sessionManager = new SessionManager(this);
        postService = new PostService();

        addPostBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MyPostsActivity.this, ActivityCreatePost.class);
            startActivity(intent);
        });

        loadMyPosts();
    }

    private void loadMyPosts() {
        String userId = sessionManager.getUserSession().getId().getId();
        DocumentReference userRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId);
        postService.getPostsByUser(userRef, posts -> {
            myPostList.clear();
            myPostList.addAll(posts);
            postAdapter.notifyDataSetChanged();
        });
    }
} 