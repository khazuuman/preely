package com.example.preely.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.preely.R;
import com.example.preely.model.entities.Transaction;
import com.example.preely.viewmodel.TransactionService;
import com.google.android.material.button.MaterialButton;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentResultActivity extends AppCompatActivity {
    
    private ImageView resultIcon, failureIcon;
    private TextView resultTitle, resultMessage;
    private TextView transactionId, transactionAmount, transactionStatus, transactionTime;
    private View transactionDetails;
    private MaterialButton btnBackToHome, btnViewTransaction;
    private ProgressBar loadingProgress;
    
    private TransactionService transactionService;
    private Transaction transaction;
    private boolean isSuccess = false;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        
        initViews();
        setupListeners();
        
        transactionService = new ViewModelProvider(this).get(TransactionService.class);
        
        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            String data = intent.getDataString();
            if (data != null) {
                // Nhận thông tin từ VNPay return URL
                String requesterId = intent.getStringExtra("requesterId");
                String giverId = intent.getStringExtra("giverId");
                String serviceId = intent.getStringExtra("serviceId");
                
                handlePaymentReturn(data, requesterId, giverId, serviceId);
            } else {
                // Nhận transaction từ VNPayActivity
                transaction = (Transaction) intent.getSerializableExtra("transaction");
                String responseCode = intent.getStringExtra("vnp_ResponseCode");
                String responseMessage = intent.getStringExtra("vnp_Message");
                
                if (transaction != null && responseCode != null) {
                    handlePaymentResult(responseCode, responseMessage);
                } else {
                    showError("Không nhận được thông tin giao dịch");
                }
            }
        } else {
            showError("Không có dữ liệu giao dịch");
        }
    }
    
    private void initViews() {
        resultIcon = findViewById(R.id.result_icon);
        failureIcon = findViewById(R.id.failure_icon);
        resultTitle = findViewById(R.id.result_title);
        resultMessage = findViewById(R.id.result_message);
        transactionId = findViewById(R.id.transaction_id);
        transactionAmount = findViewById(R.id.transaction_amount);
        transactionStatus = findViewById(R.id.transaction_status);
        transactionTime = findViewById(R.id.transaction_time);
        transactionDetails = findViewById(R.id.transaction_details);
        btnBackToHome = findViewById(R.id.btn_back_to_home);
        btnViewTransaction = findViewById(R.id.btn_view_transaction);
        loadingProgress = findViewById(R.id.loading_progress);
    }
    
    private void setupListeners() {
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (isSuccess) {
                intent.putExtra("toast_mess", "Thanh toán thành công!");
            } else {
                intent.putExtra("toast_mess", "Thanh toán thất bại hoặc bị hủy!");
            }
            startActivity(intent);
            finish();
        });
        
        btnViewTransaction.setOnClickListener(v -> {
            // Có thể mở activity xem danh sách giao dịch
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void handlePaymentReturn(String url, String requesterId, String giverId, String serviceId) {
        Log.d("PaymentResultActivity", "Handling payment return URL: " + url);
        
        Uri uri = Uri.parse(url);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");
        String responseMessage = uri.getQueryParameter("vnp_Message");
        String txnRef = uri.getQueryParameter("vnp_TxnRef");
        String amount = uri.getQueryParameter("vnp_Amount");
        String bankCode = uri.getQueryParameter("vnp_BankCode");
        String payDate = uri.getQueryParameter("vnp_PayDate");
        
        Log.d("PaymentResultActivity", "Response Code: " + responseCode);
        Log.d("PaymentResultActivity", "Response Message: " + responseMessage);
        Log.d("PaymentResultActivity", "Transaction Ref: " + txnRef);
        Log.d("PaymentResultActivity", "Amount: " + amount);
        Log.d("PaymentResultActivity", "Bank Code: " + bankCode);
        Log.d("PaymentResultActivity", "Pay Date: " + payDate);
        Log.d("PaymentResultActivity", "Requester ID: " + requesterId);
        Log.d("PaymentResultActivity", "Giver ID: " + giverId);
        Log.d("PaymentResultActivity", "Service ID: " + serviceId);

        // Nếu không thành công, chỉ show lỗi và không gọi processPaymentResult
        if (!"00".equals(responseCode)) {
            handlePaymentResult(responseCode, responseMessage);
            return;
        }

        // Chỉ xử lý transaction khi thành công
        transactionService.processPaymentResult(responseCode, responseMessage, txnRef, amount, 
            requesterId, giverId, serviceId, new TransactionService.TransactionCallback() {
                @Override
                public void onSuccess(Transaction resultTransaction) {
                    transaction = resultTransaction;
                    handlePaymentResult(responseCode, responseMessage);
                }
                @Override
                public void onError(String error) {
                    Log.e("PaymentResultActivity", "Error processing payment result: " + error);
                    showError("Lỗi xử lý kết quả thanh toán: " + error);
                }
            });
    }
    
    private void handlePaymentResult(String responseCode, String responseMessage) {
        isSuccess = "00".equals(responseCode);
        
        if (isSuccess) {
            showSuccess();
        } else {
            showFailure(responseMessage);
        }
    }
    
    private void showSuccess() {
        resultIcon.setVisibility(View.VISIBLE);
        failureIcon.setVisibility(View.GONE);
        resultTitle.setText("Thanh toán thành công!");
        resultMessage.setText("Giao dịch của bạn đã được xử lý thành công.");
        resultTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        resultMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        
        showTransactionDetails();
    }
    
    private void showFailure(String message) {
        resultIcon.setVisibility(View.GONE);
        failureIcon.setVisibility(View.VISIBLE);
        resultTitle.setText("Thanh toán thất bại");
        resultMessage.setText(message != null ? message : "Giao dịch không thành công. Vui lòng thử lại.");
        resultTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        resultMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        
        showTransactionDetails();
    }
    
    private void showError(String error) {
        resultIcon.setVisibility(View.GONE);
        failureIcon.setVisibility(View.VISIBLE);
        resultTitle.setText("Lỗi");
        resultMessage.setText(error);
        resultTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        resultMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        
        loadingProgress.setVisibility(View.GONE);
        btnBackToHome.setVisibility(View.VISIBLE);
        btnViewTransaction.setVisibility(View.GONE);
    }
    
    private void showTransactionDetails() {
        if (transaction != null) {
            transactionDetails.setVisibility(View.VISIBLE);
            
            transactionId.setText(transaction.getId() != null ? (CharSequence) transaction.getId() : "N/A");
            transactionAmount.setText(String.format("%,.0f VND", transaction.getAmount()));
            transactionStatus.setText(transaction.getStatus());
            
            String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            transactionTime.setText(currentTime);
        }
        
        loadingProgress.setVisibility(View.GONE);
        btnBackToHome.setVisibility(View.VISIBLE);
        btnViewTransaction.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (isSuccess) {
            intent.putExtra("toast_mess", "Thanh toán thành công!");
        } else {
            intent.putExtra("toast_mess", "Thanh toán thất bại hoặc bị hủy!");
        }
        startActivity(intent);
        finish();
    }
}