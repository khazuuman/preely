package com.example.preely.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.adapter.CategoryAdapter;
import com.example.preely.adapter.TransactionAdapter;
import com.example.preely.adapter.UserAdapter;
import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Transaction;
import com.example.preely.model.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFilterUtil {

    public interface SearchFilterCallback<T> {
        void onFiltered(List<T> filteredList);
    }

    public static void setupUserSearch(EditText searchEditText, List<User> originalList, 
                                     UserAdapter adapter, SearchFilterCallback<User> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<User> filteredList = originalList.stream()
                    .filter(user -> user.getFull_name() != null && 
                                  user.getFull_name().toLowerCase().contains(query) ||
                                  user.getEmail() != null && 
                                  user.getEmail().toLowerCase().contains(query) ||
                                  user.getUsername() != null && 
                                  user.getUsername().toLowerCase().contains(query) ||
                                  user.getPhone_number() != null && 
                                  user.getPhone_number().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setUserList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void setupTransactionSearch(EditText searchEditText, List<Transaction> originalList, 
                                            TransactionAdapter adapter, SearchFilterCallback<Transaction> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Transaction> filteredList = originalList.stream()
                    .filter(transaction -> transaction.getId() != null && 
                                         transaction.getId().toString().toLowerCase().contains(query) ||
                                         transaction.getStatus() != null && 
                                         transaction.getStatus().toLowerCase().contains(query) ||
                                         transaction.getGiver_id() != null && 
                                         transaction.getGiver_id().toLowerCase().contains(query) ||
                                         transaction.getRequester_id() != null && 
                                         transaction.getRequester_id().toLowerCase().contains(query) ||
                                         transaction.getService_id() != null && 
                                         transaction.getService_id().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setTransactionList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void setupCategorySearch(EditText searchEditText, List<Category> originalList, 
                                         CategoryAdapter adapter, SearchFilterCallback<Category> callback) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Category> filteredList = originalList.stream()
                    .filter(category -> category.getName() != null && 
                                      category.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                
                adapter.setCategoryList(filteredList);
                if (callback != null) {
                    callback.onFiltered(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Filter by status for transactions
    public static List<Transaction> filterTransactionsByStatus(List<Transaction> originalList, String status) {
        if (status == null || status.isEmpty() || status.equalsIgnoreCase("All")) {
            return originalList;
        }
        return originalList.stream()
            .filter(transaction -> transaction.getStatus() != null && 
                                 transaction.getStatus().equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

    // Sort functions
    public static List<User> sortUsersByName(List<User> users, boolean ascending) {
        List<User> sortedList = new ArrayList<>(users);
        if (ascending) {
            sortedList.sort((u1, u2) -> {
                String name1 = u1.getFull_name() != null ? u1.getFull_name() : "";
                String name2 = u2.getFull_name() != null ? u2.getFull_name() : "";
                return name1.compareToIgnoreCase(name2);
            });
        } else {
            sortedList.sort((u1, u2) -> {
                String name1 = u1.getFull_name() != null ? u1.getFull_name() : "";
                String name2 = u2.getFull_name() != null ? u2.getFull_name() : "";
                return name2.compareToIgnoreCase(name1);
            });
        }
        return sortedList;
    }
} 