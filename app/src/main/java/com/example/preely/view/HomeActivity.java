package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;

public class HomeActivity extends AppCompatActivity {

    TextView homeText;
    Button btnCreateTransaction;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        SessionManager sessionManager = new SessionManager(getApplicationContext());
//        if (!sessionManager.getLogin()) {
//            sessionManager.clearSession();
//            startActivity(new Intent(this, Login.class));
//            finish();
//        }

        homeText = findViewById(R.id.homeText);
        homeText.setOnClickListener(v -> {
            Log.i("user session", sessionManager.getUserId());
            // Open the page to create an escrow transaction
            Intent intent = new Intent(this, TransactionActivity.class);
            startActivity(intent);
        });

        btnCreateTransaction = findViewById(R.id.btn_create_transaction);
        btnCreateTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionActivity.class);
            startActivity(intent);
        });
    }

    // Remove onActivityResult because PaymentHelper is no longer used
}