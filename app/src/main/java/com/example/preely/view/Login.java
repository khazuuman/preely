package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.UserRequest;
import com.example.preely.util.Constraints.*;
import com.example.preely.viewmodel.UserViewModel;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
    TextInputLayout usernameTil;
    TextInputLayout passwordTil;
    com.google.android.material.button.MaterialButton loginBtn;
    UserViewModel userViewModel;
    TextView usernameErrorTv;
    TextView passwordErrorTv;
    TextInputEditText usernameInput;
    TextInputEditText passwordInput;
    MaterialCheckBox rememberCb;

    @SuppressLint({"MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.getLogin() && sessionManager.getUserId() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);



        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        usernameTil = findViewById(R.id.username_til);
        passwordTil = findViewById(R.id.password_til);
        loginBtn = findViewById(R.id.login_btn);
        usernameErrorTv = findViewById(R.id.username_error_tv);
        passwordErrorTv = findViewById(R.id.password_error_tv);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        rememberCb = findViewById(R.id.remember_cb);

        userViewModel.getUsernameError().observe(this, usernameError -> {
            if (usernameError != null) {
                usernameErrorTv.setText(usernameError);
                usernameErrorTv.setVisibility(View.VISIBLE);
            } else {
                usernameErrorTv.setVisibility(View.GONE);
            }
        });
        userViewModel.getPasswordError().observe(this, passwordError -> {
            if (passwordError != null) {
                passwordErrorTv.setText(passwordError);
                passwordErrorTv.setVisibility(View.VISIBLE);
            } else {
                passwordErrorTv.setVisibility(View.GONE);
            }
        });

        userViewModel.getLoginResult().observe(this, user -> {
            if (user != null) {
                rememberCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                   if (isChecked) {
                       sessionManager.setSessionTimeOut(TimeUnit.DAYS.toMillis(7));
                   } else {
                       sessionManager.setSessionTimeOut(0);
                   }
                });
                sessionManager.setLogin(true);
                Log.i("user id", user.getId());
                sessionManager.setUserId(user.getId());
                CustomToast.makeText(Login.this, "Login Successful", CustomToast.LENGTH_SHORT, NotificationType.SUCCESS).show();
                startActivity(new Intent(this, HomeActivity.class));
                finishAffinity();
            } else {
                CustomToast.makeText(Login.this, "Username or password incorrect", CustomToast.LENGTH_SHORT, NotificationType.ERROR).show();
            }
        });

//      input tracking
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    usernameErrorTv.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    passwordErrorTv.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        loginBtn.setOnClickListener(v -> {
            String username = Objects.requireNonNull(usernameTil.getEditText()).getText().toString();
            String password = Objects.requireNonNull(passwordTil.getEditText()).getText().toString();
            UserRequest request = new UserRequest(username, password);
            userViewModel.loginByUsername(request);
        });
    }

    //  clear focus when click outside
    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            android.view.View view = getCurrentFocus();
            if (view instanceof android.widget.EditText) {
                android.graphics.Rect outRect = new android.graphics.Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    view.clearFocus();
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}