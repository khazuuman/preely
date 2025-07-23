package com.example.preely.authentication;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DetectActivity extends Application implements Application.ActivityLifecycleCallbacks {
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityReferences--;
        if (activityReferences == 0 && !isActivityChangingConfigurations) {
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            Log.d("DetectActivity", "App backgrounded, getRemember: " + sessionManager.getRemember());
            if (!sessionManager.getRemember()) {
                sessionManager.clearSession();
                Log.d("DetectActivity", "Session cleared because !remember");
            }
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activityReferences++;
        isActivityChangingConfigurations = false;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }


    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
