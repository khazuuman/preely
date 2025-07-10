package com.example.preely.authentication;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("AppKey", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void setLogin(boolean login) {
        editor.putBoolean("KEY_LOGIN", login);
        editor.apply();
    }

    public boolean getLogin() {
        return sharedPreferences.getBoolean("KEY_LOGIN", false);
    }

    public void setUserId(String userId) {
        editor.putString("KEY_USER_ID", userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString("KEY_USER_ID", null);
    }

    public void setSessionTimeOut(long timeOut) {
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime + timeOut;
        editor.putLong("KEY_SESSION_TIME_OUT", expireTime);
        editor.putBoolean("KEY_LOGIN", true);
        editor.apply();
    }

    public boolean isSessionExpired() {
        long expireTime = sharedPreferences.getLong("KEY_SESSION_TIME_OUT", 0);
        long currentTime = System.currentTimeMillis();
        return !getLogin() && currentTime > expireTime;
    }

    public void clearSession() {
        editor.remove("KEY_USER_ID");
        editor.remove("KEY_LOGIN");
        editor.apply();
    }
}
