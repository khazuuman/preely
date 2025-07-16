package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.UserRequest;
import com.example.preely.util.Constraints.NotificationType;
import com.example.preely.viewmodel.LoginService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.github.nkzawa.socketio.client.Socket;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
    private TextInputLayout usernameTil;
    private TextInputLayout passwordTil;
    private MaterialButton loginBtn;
    private LoginService loginService;
    private TextView usernameErrorTv;
    private TextView passwordErrorTv;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private MaterialCheckBox rememberCb;
    private SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.getLogin()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        // Initialize views
        usernameTil = findViewById(R.id.username_til);
        passwordTil = findViewById(R.id.password_til);
        loginBtn = findViewById(R.id.login_btn);
        usernameErrorTv = findViewById(R.id.username_error_tv);
        passwordErrorTv = findViewById(R.id.password_error_tv);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        rememberCb = findViewById(R.id.remember_cb);

        // Initialize ViewModel
        loginService = new ViewModelProvider(this).get(LoginService.class);

        // Observe validation errors
        loginService.getUsernameError().observe(this, usernameError -> {
            if (usernameError != null) {
                usernameErrorTv.setText(usernameError);
                usernameErrorTv.setVisibility(View.VISIBLE);
            } else {
                usernameErrorTv.setVisibility(View.GONE);
            }
        });

        loginService.getPasswordError().observe(this, passwordError -> {
            if (passwordError != null) {
                passwordErrorTv.setText(passwordError);
                passwordErrorTv.setVisibility(View.VISIBLE);
            } else {
                passwordErrorTv.setVisibility(View.GONE);
            }
        });

        // Observe login result
        loginService.getLoginResult().observe(this, userResponse -> {
            if (userResponse != null) {
                sessionManager.setUserId(userResponse.getId());
                CustomToast.makeText(this, "Login Successful", CustomToast.LENGTH_SHORT, NotificationType.SUCCESS).show();

                // Khởi tạo Socket kết nối
                Socket socket = SocketManager.getSocket();
                socket.on(Socket.EVENT_CONNECT, args -> {
                    // Join với user ID
                    socket.emit("join", sessionManager.getUserId());
                    Log.d("Socket", "Connected and joined user: " + sessionManager.getUserId());
                });
                socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                    Log.e("Socket", "Connection error: " + args[0]);
                });

                startActivity(new Intent(this, HomeActivity.class));
                finishAffinity();
            } else {
                CustomToast.makeText(this, "Invalid username or password", CustomToast.LENGTH_SHORT, NotificationType.ERROR).show();
            }
        });

        // Set remember me checkbox listener
        rememberCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setSessionTimeOut(isChecked ? TimeUnit.DAYS.toMillis(7) : 0);
            sessionManager.setRemember(isChecked);
        });

        // Handle login button click
        loginBtn.setOnClickListener(v -> {
            UserRequest request = new UserRequest();
            request.setUsername(Objects.requireNonNull(usernameInput.getText()).toString());
            request.setPassword(Objects.requireNonNull(passwordInput.getText()).toString());
            loginService.loginByUsername(request);
        });

        // Input tracking for real-time error clearing
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
    }

    // Clear focus when clicking outside
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof TextInputEditText) {
                android.graphics.Rect outRect = new android.graphics.Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.disconnect();
    }
}