package com.example.preely.authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.preely.model.response.UserResponse;
import com.example.preely.util.DataUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private final Gson gson = DataUtil.buildGsonAccountSession();

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("AppKey", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public boolean getLogin() {
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        return isLoggedIn && getUserSession() != null && !isSessionExpired();
        //return isLoggedIn && getUserSession() != null && !isSessionExpired() && getRemember();
    }

    //    user information
    public void setUserSession(UserResponse user) {
        if (user != null) {
            String userJson = gson.toJson(user);
            editor.putString("user", userJson);
            editor.apply();
        }
    }

    public UserResponse getUserSession() {
        try {
            String userJson = sharedPreferences.getString("user", null);
            if (userJson != null) {
                return gson.fromJson(userJson, UserResponse.class);
            }
        } catch (Exception e) {
            Log.e("SessionManager", "Error parsing user session: " + e.getMessage());
            editor.remove("user").apply();
        }
        return null;
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
        editor.remove("user");
        editor.remove("is_logged_in");
        editor.apply();
        Log.d("SessionManager", "Session cleared");
    }

    //    remember user
    public void setRemember(boolean remember) {
        editor.putBoolean("KEY_REMEMBER", remember);
        editor.apply();
    }

    public boolean getRemember() {
        return sharedPreferences.getBoolean("KEY_REMEMBER", false);
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean("is_logged_in", isLoggedIn);
        editor.apply();
        Log.d("SessionManager", "setLogin: " + isLoggedIn);
    }

}