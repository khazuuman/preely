package com.example.preely.util;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import okhttp3.OkHttpClient;

public class StoreService {

    private static final String ACCESS_KEY = "189f1cbbc5f45f2f71bc0c64b3ce19d8";
    private static final String SECRET_KEY = "a322607dea037bad0f052c59f9559aa2fd6d778af759a104a90a9cbc4391d993";
    private static final String ACCOUNT_ID = "c7817c861c33bda236f1aee1b5139aa3";
    private static final String BUCKET_NAME = "preely";
    private static final String PUBLIC_URL = "https://pub-9a2072b17bdc4585bb5660f39c3d066a.r2.dev/";

    private final MinioClient minioClient;
    private final Context context;

    public StoreService(Context context) {
        this.context = context.getApplicationContext();

        OkHttpClient httpClient = new OkHttpClient.Builder().build();

        this.minioClient = MinioClient.builder()
                .endpoint(ACCOUNT_ID + ".r2.cloudflarestorage.com")
                .credentials(ACCESS_KEY, SECRET_KEY)
                .httpClient(httpClient)
                .build();
    }

    public CompletableFuture<String> uploadFileAsync(Uri fileUri, String keyPrefix, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = context.getContentResolver().openInputStream(fileUri)) {
                if (stream == null) {
                    throw new IllegalStateException("Cannot open InputStream from Uri");
                }

                String ext = getExtension(fileUri);
                String objectKey = keyPrefix + "/" + fileName + ext;

                PutObjectArgs args = PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectKey)
                        .stream(stream, -1, 10 * 1024 * 1024) // 10MB part size
                        .contentType(getMimeType(ext))
                        .build();

                minioClient.putObject(args);

                return PUBLIC_URL.replaceAll("/+$", "") + "/" + objectKey;
            } catch (MinioException me) {
                throw new RuntimeException("Upload failed: " + me.getMessage(), me);
            } catch (Exception e) {
                throw new RuntimeException("Upload failed: " + e.getMessage(), e);
            }
        });
    }

    private String getExtension(Uri uri) {
        String ext = null;
        String type = context.getContentResolver().getType(uri);
        if (type != null) {
            ext = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
        }

        if (ext == null || ext.equals(".")) {
            String path = uri.getPath();
            if (path != null && path.contains(".")) {
                ext = path.substring(path.lastIndexOf("."));
            } else {
                ext = "";
            }
        }
        return ext;
    }

    private String getMimeType(String ext) {
        if (ext == null || ext.isEmpty()) return "application/octet-stream";
        String noDot = ext.replace(".", "");
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(noDot);
        return type != null ? type : "application/octet-stream";
    }
}
