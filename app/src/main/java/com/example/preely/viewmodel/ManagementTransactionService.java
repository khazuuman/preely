package com.example.preely.viewmodel;

import androidx.lifecycle.Observer;
import com.example.preely.model.entities.Transaction;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.List;

public class ManagementTransactionService {
    private final MainRepository<Transaction> transactionRepository = new MainRepository<>(Transaction.class, "transaction");
    private final FirestoreRealtimeUtil realtimeUtil = new FirestoreRealtimeUtil();
    private ListenerRegistration transactionListener;

    public void getAllTransactions(Observer<List<Transaction>> observer) {
        Query query = FirebaseFirestore.getInstance().collection("transaction");
        transactionRepository.getAll(query).observeForever(observer);
    }

    public void listenRealtime(FirestoreRealtimeUtil.RealtimeListener<Transaction> listener) {
        transactionListener = realtimeUtil.listenToTransactions(listener);
    }

    public void removeRealtimeListener() {
        if (transactionListener != null) transactionListener.remove();
        realtimeUtil.removeAllListeners();
    }
} 