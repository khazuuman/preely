package com.example.preely.util;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

@com.bumptech.glide.annotation.GlideModule
public class PreelyGlideModule extends com.bumptech.glide.module.AppGlideModule {
    private static final String TAG = "PreelyGlideModule";

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Tạo OkHttpClient với timeout dài hơn
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Tăng từ 10s lên 30s
                .readTimeout(30, TimeUnit.SECONDS)     // Tăng từ 10s lên 30s
                .writeTimeout(30, TimeUnit.SECONDS)    // Tăng từ 10s lên 30s
                .retryOnConnectionFailure(true)        // Tự động retry khi lỗi kết nối
                .build();

        // Đăng ký OkHttpClient cho Glide
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Cấu hình logging để debug
        builder.setLogLevel(Log.ERROR);
    }
} 