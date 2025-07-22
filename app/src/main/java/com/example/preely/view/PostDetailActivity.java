package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.SavedPostRequest;
import com.example.preely.model.response.PostResponse;
import com.example.preely.viewmodel.PostService;

import java.util.ArrayList;
import java.util.Arrays;

public class PostDetailActivity extends AppCompatActivity {

    ImageSlider imageSlider;
    TextView postTile, postPrice, viewCount, wardPost, provincePost, postDescription;
    Button favoriteBtn, bookingBtn;
    ImageView backToHomeBtn;
    PostResponse postResponse;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_detail);

        imageSlider = findViewById(R.id.imageSlider);
        postTile = findViewById(R.id.postTile);
        postPrice = findViewById(R.id.postPrice);
        viewCount = findViewById(R.id.viewCount);
        wardPost = findViewById(R.id.wardPost);
        provincePost = findViewById(R.id.provincePost);
        postDescription = findViewById(R.id.postDescription);
        favoriteBtn = findViewById(R.id.favoriteBtn);
        bookingBtn = findViewById(R.id.booking_btn);
        backToHomeBtn = findViewById(R.id.back_to_post_list);

        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        PostService postService = new PostService();
        assert postId != null;
        postService.getPost(postId);
        postService.getPostResult().observe(this, result -> {
            postResponse = result;
            if (result != null) {
                ArrayList<SlideModel> imageList = new ArrayList<>();
                if (result.getImage() != null) {
                    for (String image : result.getImage()) {
                        imageList.add(new SlideModel(image, ScaleTypes.FIT));
                    }
                }
                imageSlider.setImageList(imageList);
                postTile.setText(result.getTitle());
                postPrice.setText("$" + result.getPrice().toString());
                viewCount.setText("(" + result.getViews_count() + " viewed)");
                wardPost.setText(result.getWard());
                provincePost.setText(result.getProvince());
                postDescription.setText(result.getDescription());
            }
        });

        favoriteBtn.setOnClickListener(v -> {
            SavedPostRequest request = new SavedPostRequest();
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            assert postResponse != null;
            request.setPost_id(postResponse.getId());
            request.setUser_id(sessionManager.getUserSession().getId());
            try {
                postService.insertSavedPost(request);
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        backToHomeBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), PostListActivity.class));
        });

        bookingBtn.setOnClickListener(v -> {
            // Handle booking button click
        });
    }
}