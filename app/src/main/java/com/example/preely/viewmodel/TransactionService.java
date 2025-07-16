package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Transaction;
import com.example.preely.model.entities.User;
import com.example.preely.model.entities.Post;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.Constraints;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionService extends ViewModel {
    
    private final MainRepository<Transaction> transactionRepository;
    private final MainRepository<User> userRepository;
    private final MainRepository<Post> postRepository;
    
    public TransactionService() {
        this.transactionRepository = new MainRepository<>(Transaction.class);
        this.userRepository = new MainRepository<>(User.class);
        this.postRepository = new MainRepository<>(Post.class);
    }
    
    /**
     * Lấy danh sách tất cả users
     */
    public LiveData<List<User>> getAllUsers() {
        Query query = FirebaseFirestore.getInstance()
            .collection(Constraints.CollectionName.USERS);
        return userRepository.getAll(query);
    }
    
    /**
     * Lấy danh sách tất cả posts
     */
    public LiveData<List<Post>> getAllPosts() {
        Query query = FirebaseFirestore.getInstance()
            .collection("post");
        return postRepository.getAll(query);
    }
    
    /**
     * Lưu transaction mới vào Firestore
     */
    public void saveTransaction(Transaction transaction, TransactionCallback callback) {
        if (transaction == null) {
            callback.onError("Transaction không hợp lệ");
            return;
        }
        
        // Cập nhật thời gian tạo
        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        transaction.setTransaction_date(currentTime);
        
        Log.d("TransactionService", "Saving transaction: " + transaction.getId());
        
        // Sử dụng document ID cụ thể thay vì auto-generate
        if (transaction.getId() != null && !transaction.getId().isEmpty()) {
            // Lưu với document ID cụ thể
            FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(transaction.getId())
                .set(transaction)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TransactionService", "Transaction saved successfully with ID: " + transaction.getId());
                    callback.onSuccess(transaction);
                })
                .addOnFailureListener(e -> {
                    Log.e("TransactionService", "Error saving transaction", e);
                    callback.onError("Lỗi lưu giao dịch: " + e.getMessage());
                });
        } else {
            // Fallback: sử dụng auto-generate ID
            transactionRepository.add(transaction, "transactions", new com.example.preely.util.DbUtil.OnInsertCallback() {
                @Override
                public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                    Log.d("TransactionService", "Transaction saved successfully with auto-generated ID: " + documentReference.getId());
                    callback.onSuccess(transaction);
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e("TransactionService", "Error saving transaction", e);
                    callback.onError("Lỗi lưu giao dịch: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Cập nhật trạng thái transaction sau khi thanh toán
     */
    public void updateTransactionStatus(String transactionId, String status, String responseCode, String responseMessage, TransactionCallback callback) {
        if (transactionId == null || status == null) {
            callback.onError("Thông tin giao dịch không hợp lệ");
            return;
        }
        
        Log.d("TransactionService", "Updating transaction status: " + transactionId + " -> " + status);
        
        // Tìm transaction hiện tại bằng document ID trước
        FirebaseFirestore.getInstance()
            .collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Transaction đã tồn tại, cập nhật trạng thái
                    Transaction existingTransaction = documentSnapshot.toObject(Transaction.class);
                    if (existingTransaction != null) {
                        existingTransaction.setStatus(status);
                        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                        existingTransaction.setTransaction_date(currentTime);
                        
                        // Lưu lại vào Firestore
                        transactionRepository.update(existingTransaction, transactionId, new com.example.preely.util.DbUtil.OnUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("TransactionService", "Transaction status updated successfully");
                                callback.onSuccess(existingTransaction);
                            }
                            
                            @Override
                            public void onFailure(Exception e) {
                                Log.e("TransactionService", "Error updating transaction status", e);
                                callback.onError("Lỗi cập nhật trạng thái: " + e.getMessage());
                            }
                        });
                    } else {
                        callback.onError("Không thể đọc thông tin giao dịch");
                    }
                } else {
                    // Thử tìm bằng field 'id' nếu không tìm thấy bằng document ID
                    Log.d("TransactionService", "Transaction not found by document ID, searching by field 'id'");
                    searchTransactionByIdField(transactionId, status, callback);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("TransactionService", "Error getting transaction by document ID", e);
                // Thử tìm bằng field 'id' nếu có lỗi
                searchTransactionByIdField(transactionId, status, callback);
            });
    }
    
    /**
     * Tạo transaction mới từ thông tin thanh toán
     */
    public Transaction createTransactionFromPayment(String txnRef, String amount, String requesterId, String giverId, String postId) {
        Transaction transaction = new Transaction();
        transaction.setId(txnRef);
        
        if (amount != null) {
            try {
                double amountValue = Double.parseDouble(amount) / 100.0; // VNPay trả về số tiền * 100
                transaction.setAmount(amountValue);
            } catch (NumberFormatException e) {
                Log.e("TransactionService", "Error parsing amount: " + amount, e);
                transaction.setAmount(0.0);
            }
        }
        
        transaction.setRequester_id(requesterId);
        transaction.setGiver_id(giverId);
        transaction.setPost_id(postId);
        transaction.setStatus("Unpaid");
        
        return transaction;
    }
    
    /**
     * Xử lý kết quả thanh toán từ VNPay
     */
    public void processPaymentResult(String responseCode, String responseMessage, String txnRef, String amount, 
                                   String requesterId, String giverId, String postId, TransactionCallback callback) {
        
        boolean isSuccess = "00".equals(responseCode);
        String status = isSuccess ? "Paid" : "Failed";
        
        Log.d("TransactionService", "Processing payment result - Code: " + responseCode + ", Status: " + status);
        
        // Tạo hoặc cập nhật transaction
        updateTransactionStatus(txnRef, status, responseCode, responseMessage, new TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                // Cập nhật thêm thông tin nếu cần
                if (transaction.getRequester_id() == null && requesterId != null) {
                    transaction.setRequester_id(requesterId);
                }
                if (transaction.getGiver_id() == null && giverId != null) {
                    transaction.setGiver_id(giverId);
                }
                if (transaction.getPost_id() == null && postId != null) {
                    transaction.setPost_id(postId);
                }
                if (transaction.getAmount() == null || transaction.getAmount() == (Number) 0) {
                    if (amount != null) {
                        try {
                            double amountValue = Double.parseDouble(amount) / 100.0;
                            transaction.setAmount(amountValue);
                        } catch (NumberFormatException e) {
                            Log.e("TransactionService", "Error parsing amount: " + amount, e);
                        }
                    }
                }
                
                // Lưu lại với thông tin đầy đủ
                saveTransaction(transaction, callback);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Lấy danh sách transaction của user
     */
    public LiveData<List<Transaction>> getUserTransactions(String userId) {
        Log.d("TransactionService", "Getting transactions for user: " + userId);
        
        Query query = FirebaseFirestore.getInstance()
            .collection("transactions")
            .whereEqualTo("requester_id", userId);
            
        return transactionRepository.getAll(query);
    }
    
    /**
     * Lấy transaction theo ID
     */
    public LiveData<Transaction> getTransactionById(String transactionId) {
        Log.d("TransactionService", "Getting transaction: " + transactionId);
        
        MutableLiveData<Transaction> result = new MutableLiveData<>();
        
        // Thử tìm bằng document ID trước
        FirebaseFirestore.getInstance()
            .collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Transaction transaction = documentSnapshot.toObject(Transaction.class);
                    result.setValue(transaction);
                } else {
                    // Thử tìm bằng field 'id'
                    Log.d("TransactionService", "Transaction not found by document ID, searching by field 'id'");
                    searchTransactionByIdFieldForGet(transactionId, result);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("TransactionService", "Error getting transaction by document ID", e);
                // Thử tìm bằng field 'id' nếu có lỗi
                searchTransactionByIdFieldForGet(transactionId, result);
            });
            
        return result;
    }
    
    /**
     * Tìm transaction bằng field 'id' cho method get
     */
    private void searchTransactionByIdFieldForGet(String transactionId, MutableLiveData<Transaction> result) {
        Log.d("TransactionService", "Searching transaction by field 'id' for get: " + transactionId);
        
        Query query = FirebaseFirestore.getInstance()
            .collection("transactions")
            .whereEqualTo("id", transactionId);
            
        transactionRepository.getOne(query).observeForever(transaction -> {
            if (transaction != null) {
                Log.d("TransactionService", "Transaction found by field 'id'");
                result.setValue(transaction);
            } else {
                Log.d("TransactionService", "Transaction not found by field 'id' either");
                result.setValue(null);
            }
        });
    }
    
    /**
     * Tìm transaction bằng field 'id'
     */
    private void searchTransactionByIdField(String transactionId, String status, TransactionCallback callback) {
        Log.d("TransactionService", "Searching transaction by field 'id': " + transactionId);
        
        Query query = FirebaseFirestore.getInstance()
            .collection("transactions")
            .whereEqualTo("id", transactionId);
            
        transactionRepository.getOne(query).observeForever(existingTransaction -> {
            if (existingTransaction != null) {
                // Tìm thấy transaction, cập nhật trạng thái
                existingTransaction.setStatus(status);
                String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                existingTransaction.setTransaction_date(currentTime);
                
                // Lưu lại vào Firestore
                transactionRepository.update(existingTransaction, existingTransaction.getId(), new com.example.preely.util.DbUtil.OnUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("TransactionService", "Transaction status updated successfully by field search");
                        callback.onSuccess(existingTransaction);
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("TransactionService", "Error updating transaction status by field search", e);
                        callback.onError("Lỗi cập nhật trạng thái: " + e.getMessage());
                    }
                });
            } else {
                // Tạo transaction mới nếu không tìm thấy
                Log.d("TransactionService", "Transaction not found by field 'id', creating new one");
                Transaction newTransaction = new Transaction();
                newTransaction.setId(transactionId);
                newTransaction.setStatus(status);
                String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                newTransaction.setTransaction_date(currentTime);
                
                saveTransaction(newTransaction, callback);
            }
        });
    }
    
    // Callback interfaces
    public interface TransactionCallback {
        void onSuccess(Transaction transaction);
        void onError(String error);
    }
} 