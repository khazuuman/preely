package com.example.preely.exception;

import android.util.Log;

import java.util.Objects;

public class ExceptionHandler {
    public static void handleException(Exception e) {
        Log.e("ExceptionHanler", Objects.requireNonNull(e.getMessage()));
    }
}
