package com.example.preely.view;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.preely.R;

public class PostListActivity extends AppCompatActivity {

    ImageView filterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_list);

        filterBtn = findViewById(R.id.filterBtn);
        filterBtn.setOnClickListener(v -> {
            FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
            filterBottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });
    }
}