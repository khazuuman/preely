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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.preely.R;
import com.example.preely.authentication.SessionManager;
import com.example.preely.model.request.UserLoginRequest;
import com.example.preely.model.response.UserResponse;
import com.example.preely.util.Constraints.NotificationType;
import com.example.preely.util.ViewUtil;
import com.example.preely.viewmodel.UserLoginService;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
    MaterialButton loginBtn, signupBtn;
    MaterialCheckBox rememberCb;
    UserLoginService userLoginService;
    TextView usernameErrorTv, passwordErrorTv;
    TextInputEditText usernameInput, passwordInput;
    SessionManager sessionManager;
    ImageView ggIcon, ggLogout, faceIcon;

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
//        test facebook token
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }

        // Initialize views
        loginBtn = findViewById(R.id.login_btn);
        signupBtn = findViewById(R.id.signup_btn);
        usernameErrorTv = findViewById(R.id.username_error_tv);
        passwordErrorTv = findViewById(R.id.password_error_tv);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        rememberCb = findViewById(R.id.remember_cb);
        ggIcon = findViewById(R.id.google_icon);
        faceIcon = findViewById(R.id.face_icon);
        ggLogout = findViewById(R.id.ggLogout_btn);

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

        // facebook setting
        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        faceIcon.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    this,
                    Arrays.asList("email", "public_profile")
            );
        });

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });


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
                CustomToast.makeText(this, "Login Successful", CustomToast.LENGTH_SHORT, NotificationType.SUCCESS).show();
                startActivity(new Intent(this, HomeActivity.class));
                finishAffinity();
            } else {
                CustomToast.makeText(this, "Invalid username or password", CustomToast.LENGTH_SHORT, NotificationType.ERROR).show();
            }
        });

        userLoginService.getUserInfo().observe(this, userResponse -> {
            if (userResponse != null) {
                sessionManager.setUserSession(userResponse);
                sessionManager.setSessionTimeOut(TimeUnit.DAYS.toMillis(7));
            }
        });

        // Set remember me checkbox listener
        rememberCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setSessionTimeOut(isChecked ? TimeUnit.DAYS.toMillis(7) : 0);
            sessionManager.setRemember(isChecked);
        });

        // Handle login button click
        loginBtn.setOnClickListener(v -> {
            UserLoginRequest request = new UserLoginRequest();
            request.setUsername(Objects.requireNonNull(usernameInput.getText()).toString());
            request.setPassword(Objects.requireNonNull(passwordInput.getText()).toString());
            userLoginService.loginByUsername(request);
        });

        signupBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUp.class));
        });

        ggIcon.setOnClickListener(v -> signInWithGoogle());
        ggLogout.setOnClickListener(v -> {
            mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
                mAuth.signOut();
                Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            });
        });

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
                            Intent intent = new Intent(this, HomeActivity.class);
                            intent.putExtra("toast_mess", "Đăng nhập thành công");
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private UserResponse convertFirebaseUserToUserResponse(FirebaseUser user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUsername(user.getDisplayName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone_number(user.getPhoneNumber());
//        userResponse.setAvatar(user.getPhotoUrl().toString());
        return userResponse;
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

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        Log.d("FB_TOKEN", token.getToken());
        Log.d("FB_APP_ID", token.getApplicationId());
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
//                                sessionManager.setUserId(user.getUid());
//                                sessionManager.setSessionTimeOut(TimeUnit.DAYS.toMillis(7));
//                                userLoginService.handleFacebookLoginDetail(user);
                                Intent intent = new Intent(Login.this, HomeActivity.class);
                                intent.putExtra("toast_mess", "Đăng nhập thành công");
                                startActivity(intent);
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            CustomToast.makeText(Login.this, "Authentication failed.", CustomToast.LENGTH_SHORT, NotificationType.ERROR).show();
                        }
                    }
                });
    }

}