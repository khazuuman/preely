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
     * Get the list of all users
     */
    public LiveData<List<User>> getAllUsers() {
        Query query = FirebaseFirestore.getInstance()
            .collection(Constraints.CollectionName.USERS);
        return userRepository.getAll(query);
    }
    
    /**
     * Get the list of all posts
     */
    public LiveData<List<Post>> getAllPosts() {
        Query query = FirebaseFirestore.getInstance()
            .collection("post");
        return postRepository.getAll(query);
    }
    
    /**
     * Save a new transaction to Firestore
     */
    public void saveTransaction(Transaction transaction, TransactionCallback callback) {
        if (transaction == null) {
            callback.onError("Invalid transaction");
            return;
        }
        
        // Update creation time
        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        transaction.setTransaction_date(currentTime);
        
        Log.d("TransactionService", "Saving transaction: " + transaction.getId());
        
        // Use specific document ID instead of auto-generate
        if (transaction.getId() != null && !transaction.getId().isEmpty()) {
            // Save with specific document ID
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
                    callback.onError("Error saving transaction: " + e.getMessage());
                });
        } else {
            // Fallback: use auto-generate ID
            transactionRepository.add(transaction, "transactions", new com.example.preely.util.DbUtil.OnInsertCallback() {
                @Override
                public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                    Log.d("TransactionService", "Transaction saved successfully with auto-generated ID: " + documentReference.getId());
                    callback.onSuccess(transaction);
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e("TransactionService", "Error saving transaction", e);
                    callback.onError("Error saving transaction: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Update transaction status after payment
     */
    public void updateTransactionStatus(String transactionId, String status, String responseCode, String responseMessage, TransactionCallback callback) {
        if (transactionId == null || status == null) {
            callback.onError("Invalid transaction information");
            return;
        }
        
        Log.d("TransactionService", "Updating transaction status: " + transactionId + " -> " + status);
        
        // Find current transaction by document ID first
        FirebaseFirestore.getInstance()
            .collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Transaction exists, update status
                    Transaction existingTransaction = documentSnapshot.toObject(Transaction.class);
                    if (existingTransaction != null) {
                        existingTransaction.setStatus(status);
                        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                        existingTransaction.setTransaction_date(currentTime);
                        
                        // Save back to Firestore
                        transactionRepository.update(existingTransaction, existingTransaction.getId(), "transactions", new com.example.preely.util.DbUtil.OnUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("TransactionService", "Transaction status updated successfully");
                                callback.onSuccess(existingTransaction);
                            }
                            
                            @Override
                            public void onFailure(Exception e) {
                                Log.e("TransactionService", "Error updating transaction status", e);
                                callback.onError("Error updating status: " + e.getMessage());
                            }
                        });
                    } else {
                        callback.onError("Could not read transaction information");
                    }
                } else {
                    // Try to find by field 'id' if not found by document ID
                    Log.d("TransactionService", "Transaction not found by document ID, searching by field 'id'");
                    searchTransactionByIdField(transactionId, status, callback);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("TransactionService", "Error getting transaction by document ID", e);
                // Try to find by field 'id' if error
                searchTransactionByIdField(transactionId, status, callback);
            });
    }
    
    /**
     * Create a new transaction from payment information
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
     * Process payment result from VNPay
     */
    public void processPaymentResult(String responseCode, String responseMessage, String txnRef, String amount, 
                                   String requesterId, String giverId, String postId, TransactionCallback callback) {
        
        boolean isSuccess = "00".equals(responseCode);
        String status = isSuccess ? "Paid" : "Failed";
        
        Log.d("TransactionService", "Processing payment result - Code: " + responseCode + ", Status: " + status);
        
        // Create or update transaction
        updateTransactionStatus(txnRef, status, responseCode, responseMessage, new TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                // Update additional information if needed
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
                
                // Save back with full information
                saveTransaction(transaction, callback);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Get the list of transactions for a user
     */
    public LiveData<List<Transaction>> getUserTransactions(String userId) {
        Log.d("TransactionService", "Getting transactions for user: " + userId);
        
        Query query = FirebaseFirestore.getInstance()
            .collection("transactions")
            .whereEqualTo("requester_id", userId);
            
        return transactionRepository.getAll(query);
    }
    
    /**
     * Get transaction by ID
     */
    public LiveData<Transaction> getTransactionById(String transactionId) {
        Log.d("TransactionService", "Getting transaction: " + transactionId);
        
        MutableLiveData<Transaction> result = new MutableLiveData<>();
        
        // Try to find by document ID first
        FirebaseFirestore.getInstance()
            .collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Transaction transaction = documentSnapshot.toObject(Transaction.class);
                    result.setValue(transaction);
                } else {
                    // Try to find by field 'id'
                    Log.d("TransactionService", "Transaction not found by document ID, searching by field 'id'");
                    searchTransactionByIdFieldForGet(transactionId, result);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("TransactionService", "Error getting transaction by document ID", e);
                // Try to find by field 'id' if error
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
                // Find transaction, update status
                existingTransaction.setStatus(status);
                String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                existingTransaction.setTransaction_date(currentTime);
                
                // Save back to Firestore
                transactionRepository.update(existingTransaction, existingTransaction.getId(), "transactions", new com.example.preely.util.DbUtil.OnUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("TransactionService", "Transaction status updated successfully by field search");
                        callback.onSuccess(existingTransaction);
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("TransactionService", "Error updating transaction status by field search", e);
                        callback.onError("Error updating status: " + e.getMessage());
                    }
                });
            } else {
                // Create new transaction if not found
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