package com.example.preely.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.adapter.TransactionAdapter;
import com.example.preely.dialog.AddEditTransactionDialog;
import com.example.preely.model.entities.Transaction;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.example.preely.util.PaginationUtil;
import com.example.preely.util.SearchFilterUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import com.example.preely.util.DbUtil;

import java.util.ArrayList;
import java.util.List;

public class TransactionManagementFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<Transaction> transactionList = new ArrayList<>();
    private List<Transaction> originalTransactionList = new ArrayList<>();
    private MainRepository<Transaction> transactionRepository;
    private TransactionAdapter transactionAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration transactionListener;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadTransactions(); // Load data first
        setupListeners();
        
        // Setup real-time listener after a short delay to ensure data is loaded
        view.post(() -> {
            if (isAdded()) { // Check if fragment is still attached
                setupRealtimeListener();
            }
        });
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_transactions);
        fabAdd = view.findViewById(R.id.fab_add_transaction);
        etSearch = view.findViewById(R.id.et_search_transactions);
        transactionRepository = new MainRepository<>(Transaction.class);
        realtimeUtil = new FirestoreRealtimeUtil();
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter();
        transactionAdapter.setOnTransactionClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(transactionAdapter);
    }

    private void setupSearch() {
        SearchFilterUtil.setupTransactionSearch(etSearch, originalTransactionList, transactionAdapter, 
            new SearchFilterUtil.SearchFilterCallback<Transaction>() {
                @Override
                public void onFiltered(List<Transaction> filteredList) {
                    transactionList.clear();
                    transactionList.addAll(filteredList);
                }
            });
    }

    private void setupRealtimeListener() {
        transactionListener = realtimeUtil.listenToTransactions(new FirestoreRealtimeUtil.RealtimeListener<Transaction>() {
            @Override
            public void onDataAdded(Transaction transaction) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Check if transaction already exists to avoid duplicate notifications
                        boolean transactionExists = originalTransactionList.stream()
                            .anyMatch(existingTransaction -> existingTransaction.getId().equals(transaction.getId()));
                        
                        if (!transactionExists) {
                            originalTransactionList.add(transaction);
                            transactionList.add(transaction);
                            transactionAdapter.setTransactionList(transactionList);
                            Toast.makeText(getContext(), "New transaction added: " + transaction.getId(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onDataModified(Transaction transaction) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateTransactionInList(originalTransactionList, transaction);
                        updateTransactionInList(transactionList, transaction);
                        transactionAdapter.setTransactionList(transactionList);
                        Toast.makeText(getContext(), "Transaction updated: " + transaction.getId(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onDataRemoved(Transaction transaction) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        removeTransactionFromList(originalTransactionList, transaction);
                        removeTransactionFromList(transactionList, transaction);
                        transactionAdapter.setTransactionList(transactionList);
                        Toast.makeText(getContext(), "Transaction removed: " + transaction.getId(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Real-time error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateTransactionInList(List<Transaction> list, Transaction updatedTransaction) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedTransaction.getId())) {
                list.set(i, updatedTransaction);
                break;
            }
        }
    }

    private void removeTransactionFromList(List<Transaction> list, Transaction transactionToRemove) {
        list.removeIf(transaction -> transaction.getId().equals(transactionToRemove.getId()));
    }

    private void loadTransactions() {
        Query query = db.collection("transaction");
        transactionRepository.getAll(query).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                originalTransactionList.clear();
                transactionList.clear();
                originalTransactionList.addAll(transactions);
                transactionList.addAll(transactions);
                transactionAdapter.setTransactionList(transactionList);
            }
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddTransactionDialog();
        });
    }

    private void showAddTransactionDialog() {
        AddEditTransactionDialog dialog = new AddEditTransactionDialog(getContext(), null, 
            new AddEditTransactionDialog.OnTransactionDialogListener() {
                @Override
                public void onTransactionSaved(Transaction transaction, boolean isEdit) {
                    if (isEdit) {
                        updateTransaction(transaction);
                    } else {
                        saveTransaction(transaction);
                    }
                }
            });
        dialog.show();
    }

    private void showEditTransactionDialog(Transaction transaction) {
        AddEditTransactionDialog dialog = new AddEditTransactionDialog(getContext(), transaction, 
            new AddEditTransactionDialog.OnTransactionDialogListener() {
                @Override
                public void onTransactionSaved(Transaction transaction, boolean isEdit) {
                    updateTransaction(transaction);
                }
            });
        dialog.show();
    }

    private void saveTransaction(Transaction transaction) {
        transactionRepository.add(transaction, "transaction", new DbUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "Transaction saved successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error saving transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTransaction(Transaction transaction) {
        transactionRepository.update(transaction, transaction.getId(), "transaction", new DbUtil.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Transaction updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error updating transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete", (dialog, which) -> {
                transactionRepository.delete(transaction.getId(), "transaction", new DbUtil.OnDeleteCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        // Show transaction details dialog
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(transaction.getId()).append("\n");
        if (transaction.getAmount() != null) {
            details.append("Amount: $").append(transaction.getAmount()).append("\n");
        }
        details.append("Status: ").append(transaction.getStatus()).append("\n");
        details.append("Giver ID: ").append(transaction.getGiver_id()).append("\n");
        details.append("Requester ID: ").append(transaction.getRequester_id()).append("\n");
        details.append("Post ID: ").append(transaction.getPost_id()).append("\n");
        if (transaction.getTransaction_date() != null) {
            details.append("Transaction Date: ").append(transaction.getTransaction_date());
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Transaction Details")
            .setMessage(details.toString())
            .setPositiveButton("Edit", (dialog, which) -> showEditTransactionDialog(transaction))
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onTransactionEdit(Transaction transaction) {
        showEditTransactionDialog(transaction);
    }

    @Override
    public void onTransactionDelete(Transaction transaction) {
        deleteTransaction(transaction);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionListener != null) {
            transactionListener.remove();
        }
        realtimeUtil.removeAllListeners();
    }
} 