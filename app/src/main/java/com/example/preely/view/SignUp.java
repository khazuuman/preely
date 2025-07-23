package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.UserRegisterRequest;
import com.example.preely.util.Constraints;
import com.example.preely.util.ViewUtil;
import com.example.preely.viewmodel.UserLoginService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SignUp extends AppCompatActivity {

    TextInputEditText usernameInput, passwordInput, passwordConfirmInput;
    TextView usernameError, passwordError, passwordConfirmError;
    MaterialButton signupBtn, loginRedirectBtn;
    UserLoginService userLoginService;
    ImageView ggIcon;
    SessionManager sessionManager;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;

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
        ggIcon = findViewById(R.id.gg_icon);

        setupGoogle();
        sessionManager = new SessionManager(getApplicationContext());

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

        userLoginService.getUserInfo().observe(this, userResponse -> {
            if (userResponse != null) {
                Log.i("USER INFO", userResponse.toString());
                sessionManager.setUserSession(userResponse);
                sessionManager.setSessionTimeOut(TimeUnit.DAYS.toMillis(7));
                sessionManager.setRemember(true);

                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("toast_mess", "Đăng nhập thành công");
                startActivity(intent);
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

        ggIcon.setOnClickListener(v -> signInWithGoogle());
    }

    public void setupGoogle() {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    //    google login
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userLoginService.handleGoogleLoginDetail(user);
                        }
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
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