package com.example.preely.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.preely.model.entities.Transaction;
import com.example.preely.util.VNPayConfig;
import com.example.preely.util.VNPayUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VNPayActivity extends AppCompatActivity {
    private WebView webView;
    private Transaction transaction;
    private double amount;
    private String orderInfo;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        amount = intent.getDoubleExtra("amount", 0.0);
        orderInfo = intent.getStringExtra("orderInfo");
        transaction = (Transaction) intent.getSerializableExtra("transaction");
        String requesterId = intent.getStringExtra("requesterId");
        String giverId = intent.getStringExtra("giverId");
        String postId = intent.getStringExtra("postId");

        // Validate amount
        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate unique transaction reference
        String txnRef = "TXN" + System.currentTimeMillis();
        
        // Tạo thời gian hiện tại và thời gian hết hạn (15 phút sau)
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String expireDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
        
        // Build các tham số VNPay theo thứ tự như code C#
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNPayConfig.TMN_CODE);
        vnp_Params.put("vnp_Amount", String.valueOf((long)(amount * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo != null ? orderInfo : "Thanh toan giao dich trung gian");
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.RETURN_URL);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", createDate);
        vnp_Params.put("vnp_ExpireDate", expireDate);

        try {
            // Log tất cả params
            System.out.println("=== VNPay Parameters ===");
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            
            // Tạo payment URL sử dụng method mới
            String paymentUrl = VNPayUtil.createPaymentUrl(vnp_Params, VNPayConfig.HASH_SECRET, VNPayConfig.PAY_URL);
            
            System.out.println("=== Final Payment URL ===");
            System.out.println(paymentUrl);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo payment URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(VNPayConfig.RETURN_URL)) {
                    // Chuyển sang PaymentResultActivity để xử lý kết quả
                    Intent resultIntent = new Intent(VNPayActivity.this, PaymentResultActivity.class);
                    resultIntent.setData(Uri.parse(url));
                    resultIntent.putExtra("requesterId", requesterId);
                    resultIntent.putExtra("giverId", giverId);
                    resultIntent.putExtra("postId", postId);
                    startActivity(resultIntent);
                    finish();
                    return true;
                }
                return false;
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith(VNPayConfig.RETURN_URL)) {
                    // Chuyển sang PaymentResultActivity để xử lý kết quả
                    Intent resultIntent = new Intent(VNPayActivity.this, PaymentResultActivity.class);
                    resultIntent.setData(Uri.parse(url));
                    resultIntent.putExtra("requesterId", requesterId);
                    resultIntent.putExtra("postId", postId);
                    resultIntent.putExtra("giverId", giverId);
                    startActivity(resultIntent);
                    finish();
                    return true;
                }
                return false;
            }
        });
        
        try {
            String paymentUrl = VNPayUtil.createPaymentUrl(vnp_Params, VNPayConfig.HASH_SECRET, VNPayConfig.PAY_URL);
            webView.loadUrl(paymentUrl);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi load payment URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


} 