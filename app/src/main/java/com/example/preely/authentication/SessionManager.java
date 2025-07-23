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
        return getUserSession() != null && !isSessionExpired() && getRemember();
    }

    //    user information
    public void setUserSession(UserResponse user) {
        if (user != null) {
            String userJson = gson.toJson(user);
            editor.putString("USER_INFO", userJson);
            Log.i("USER SESSION", userJson);
            editor.apply();
        }
    }

    public UserResponse getUserSession() {
        String userJson = sharedPreferences.getString("USER_INFO", null);
        Log.i("USER SESSION", userJson == null ? "null" : userJson);
        if (userJson != null) {
            Log.i("USER RESPONSE SESSION", gson.fromJson(userJson, UserResponse.class).toString());
            return gson.fromJson(userJson, UserResponse.class);
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
        editor.remove("USER_INFO");
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
