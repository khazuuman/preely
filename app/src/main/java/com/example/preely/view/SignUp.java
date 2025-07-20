package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;
import com.example.preely.model.request.UserRegisterRequest;
import com.example.preely.util.Constraints;
import com.example.preely.util.ViewUtil;
import com.example.preely.viewmodel.UserLoginService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    TextInputEditText usernameInput, passwordInput, passwordConfirmInput;
    TextView usernameError, passwordError, passwordConfirmError;
    MaterialButton signupBtn, loginRedirectBtn;
    UserLoginService userLoginService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        passwordConfirmInput = findViewById(R.id.password_confirm_input);
        signupBtn = findViewById(R.id.signup_btn);
        loginRedirectBtn = findViewById(R.id.login_redirect_btn);
        usernameError = findViewById(R.id.username_error_tv);
        passwordError = findViewById(R.id.password_error_tv);
        passwordConfirmError = findViewById(R.id.password_confirm_error_tv);

        userLoginService = new ViewModelProvider(this).get(UserLoginService.class);

        userLoginService.getUsernameError().observe(this, usernameError -> {
            if (usernameError != null) {
                this.usernameError.setText(usernameError);
                this.usernameError.setVisibility(View.VISIBLE);
            } else {
                this.usernameError.setVisibility(View.GONE);
            }
        });

        userLoginService.getPasswordError().observe(this, passwordError -> {
            if (passwordError != null) {
                this.passwordError.setText(passwordError);
                this.passwordError.setVisibility(View.VISIBLE);
            } else {
                this.passwordError.setVisibility(View.GONE);
            }
        });

        userLoginService.getConfirmPasswordError().observe(this, passwordConfirmError -> {
            if (passwordConfirmError != null) {
                this.passwordConfirmError.setText(passwordConfirmError);
                this.passwordConfirmError.setVisibility(View.VISIBLE);
            } else {
                this.passwordConfirmError.setVisibility(View.GONE);
            }
        });

        userLoginService.getSignupResult().observe(this, result -> {
            if (result) {
                CustomToast.makeText(this, "Sign Up Successful", CustomToast.LENGTH_SHORT, Constraints.NotificationType.SUCCESS).show();
                startActivity(new Intent(this, Login.class));
                finish();
            } else {
                CustomToast.makeText(this, "Sign Up Failed", CustomToast.LENGTH_SHORT, Constraints.NotificationType.ERROR).show();
            }
        });

        signupBtn.setOnClickListener(v -> {
            UserRegisterRequest request = new UserRegisterRequest();
            request.setUsername(Objects.requireNonNull(usernameInput.getText()).toString());
            request.setPassword(Objects.requireNonNull(passwordInput.getText()).toString());
            request.setConfirmPassword(Objects.requireNonNull(passwordConfirmInput.getText()).toString());
            userLoginService.registerByUsername(request);
        });

        ViewUtil.clearErrorOnTextChanged(usernameInput, usernameError);
        ViewUtil.clearErrorOnTextChanged(passwordInput, passwordError);
        ViewUtil.clearErrorOnTextChanged(passwordConfirmInput, passwordConfirmError);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null) {
                ViewUtil.hideKeyboardIfTouchOutside(view, ev, this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}