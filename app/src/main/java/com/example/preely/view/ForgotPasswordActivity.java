package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.preely.R;
import com.example.preely.view.fragment.ForgotPassStFragment;
import com.example.preely.viewmodel.ForgotPasswordService;

public class ForgotPasswordActivity extends AppCompatActivity {

    ForgotPasswordService forgotPasswordService;
    ImageView backToLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        backToLogin = findViewById(R.id.back_to_login);

        forgotPasswordService = new ForgotPasswordService();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_forgot_pass_st, new ForgotPassStFragment())
                    .commit();
        }

        backToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}