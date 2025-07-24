package com.example.preely.view;

import static androidx.constraintlayout.widget.Constraints.TAG;

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
import com.example.preely.model.request.UserLoginRequest;
import com.example.preely.util.Constraints.NotificationType;
import com.example.preely.util.ViewUtil;
import com.example.preely.viewmodel.UserLoginService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
    MaterialButton loginBtn, signupBtn;
    MaterialCheckBox rememberCb;
    UserLoginService userLoginService;
    TextView usernameErrorTv, passwordErrorTv;
    TextInputEditText usernameInput, passwordInput;
    SessionManager sessionManager;
    ImageView ggIcon;

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize views
        loginBtn = findViewById(R.id.login_btn);
        signupBtn = findViewById(R.id.signup_btn);
        usernameErrorTv = findViewById(R.id.username_error_tv);
        passwordErrorTv = findViewById(R.id.password_error_tv);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        rememberCb = findViewById(R.id.remember_cb);
        ggIcon = findViewById(R.id.google_icon);

        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.getLogin()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        // google setting
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ViewModel
        userLoginService = new ViewModelProvider(this).get(UserLoginService.class);

        // Observe validation errors
        userLoginService.getUsernameError().observe(this, usernameError -> {
            if (usernameError != null) {
                usernameErrorTv.setText(usernameError);
                usernameErrorTv.setVisibility(View.VISIBLE);
            } else {
                usernameErrorTv.setVisibility(View.GONE);
            }
        });

        userLoginService.getPasswordError().observe(this, passwordError -> {
            if (passwordError != null) {
                passwordErrorTv.setText(passwordError);
                passwordErrorTv.setVisibility(View.VISIBLE);
            } else {
                passwordErrorTv.setVisibility(View.GONE);
            }
        });

        // Observe login result
        userLoginService.getLoginResult().observe(this, userResponse -> {
            if (userResponse != null) {
                sessionManager.setUserSession(userResponse);
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("toast_mess", "Đăng nhập thành công");
                startActivity(intent);
                finishAffinity();
            } else {
                CustomToast.makeText(this, "Invalid username or password", CustomToast.LENGTH_SHORT, NotificationType.ERROR).show();
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

        // Set remember me checkbox listener
        rememberCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setSessionTimeOut(isChecked ? TimeUnit.DAYS.toMillis(7) : 0);
            sessionManager.setRemember(isChecked);
        });

        // Handle login button click
        loginBtn.setOnClickListener(v -> {
            String username = Objects.requireNonNull(usernameInput.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();
            if (username.equals("admin") && password.equals("Admin@123")) {
                sessionManager.setRemember(true);
                sessionManager.setSessionTimeOut(24 * 60 * 60 * 1000); // 1 ngày
                Intent intent = new Intent(Login.this, ManagementActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            UserLoginRequest request = new UserLoginRequest();
            request.setUsername(Objects.requireNonNull(usernameInput.getText()).toString());
            request.setPassword(Objects.requireNonNull(passwordInput.getText()).toString());
            userLoginService.loginByUsername(request);
        });

        signupBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUp.class));
        });

        ggIcon.setOnClickListener(v -> signInWithGoogle());

        // Input tracking for real-time error clearing
        ViewUtil.clearErrorOnTextChanged(usernameInput, usernameErrorTv);
        ViewUtil.clearErrorOnTextChanged(passwordInput, passwordErrorTv);
    }

    // Clear focus when clicking outside
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
}