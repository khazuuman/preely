package com.example.preely.authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("AppKey", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public boolean getLogin() {
        return getUserId() != null && !isSessionExpired() && getRemember();
    }

//    user information
    public void setUserId(String userId) {
        editor.putString("KEY_USER_ID", userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString("KEY_USER_ID", null);
    }

//    session duration
    public void setSessionTimeOut(long timeOut) {
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime + timeOut;
        editor.putLong("KEY_SESSION_TIME_OUT", expireTime);
        editor.apply();
    }

    public boolean isSessionExpired() {
        long expireTime = sharedPreferences.getLong("KEY_SESSION_TIME_OUT", 0);
        long currentTime = System.currentTimeMillis();
        return currentTime > expireTime;
    }

//    clear session
    public void clearSession() {
        editor.remove("KEY_USER_ID");
        editor.remove("KEY_SESSION_TIME_OUT");
        editor.remove("KEY_REMEMBER");
        editor.apply();
    }

//    remember user
    public void setRemember(boolean remember) {
        editor.putBoolean("KEY_REMEMBER", remember);
        editor.apply();
    }

    public boolean getRemember() {
        return sharedPreferences.getBoolean("KEY_REMEMBER", false);
    }
}
