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

import com.example.preely.util.CallBackUtil;
import com.example.preely.util.Constraints.*;
import com.example.preely.R;
import com.example.preely.adapter.UserAdapter;
import com.example.preely.dialog.AddEditUserDialog;
import com.example.preely.model.entities.User;
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

public class UserManagementFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<User> userList = new ArrayList<>();
    private List<User> originalUserList = new ArrayList<>();
    private MainRepository<User> userRepository;
    private UserAdapter userAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration userListener;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadUsers(); // Load data first
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
        recyclerView = view.findViewById(R.id.recycler_users);
        fabAdd = view.findViewById(R.id.fab_add_user);
        etSearch = view.findViewById(R.id.et_search_users);
        userRepository = new MainRepository<>(User.class, CollectionName.USERS);
        realtimeUtil = new FirestoreRealtimeUtil();
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter();
        userAdapter.setOnUserClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(userAdapter);
    }

    private void setupSearch() {
        SearchFilterUtil.setupUserSearch(etSearch, originalUserList, userAdapter, 
            new SearchFilterUtil.SearchFilterCallback<User>() {
                @Override
                public void onFiltered(List<User> filteredList) {
                    userList.clear();
                    userList.addAll(filteredList);
                }
            });
    }

    private void setupRealtimeListener() {
        userListener = realtimeUtil.listenToUsers(new FirestoreRealtimeUtil.RealtimeListener<User>() {
            @Override
            public void onDataAdded(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Check if user already exists to avoid duplicate notifications
                        boolean userExists = originalUserList.stream()
                            .anyMatch(existingUser -> existingUser.getId().equals(user.getId()));
                        
                        if (!userExists) {
                            originalUserList.add(user);
                            userList.add(user);
                            userAdapter.setUserList(userList);
                            Toast.makeText(getContext(), "New user added: " + user.getFull_name(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onDataModified(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update in both lists
                        updateUserInList(originalUserList, user);
                        updateUserInList(userList, user);
                        userAdapter.setUserList(userList);
                        Toast.makeText(getContext(), "User updated: " + user.getFull_name(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onDataRemoved(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Remove from both lists
                        removeUserFromList(originalUserList, user);
                        removeUserFromList(userList, user);
                        userAdapter.setUserList(userList);
                        Toast.makeText(getContext(), "User removed: " + user.getFull_name(), Toast.LENGTH_SHORT).show();
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

    private void updateUserInList(List<User> list, User updatedUser) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedUser.getId())) {
                list.set(i, updatedUser);
                break;
            }
        }
    }

    private void removeUserFromList(List<User> list, User userToRemove) {
        list.removeIf(user -> user.getId().equals(userToRemove.getId()));
    }

    private void loadUsers() {
        Query query = db.collection("user");
        userRepository.getAll(query).observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                originalUserList.clear();
                userList.clear();
                originalUserList.addAll(users);
                userList.addAll(users);
                userAdapter.setUserList(userList);
            }
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddUserDialog();
        });
    }

    private void showAddUserDialog() {
        AddEditUserDialog dialog = new AddEditUserDialog(getContext(), null, 
            new AddEditUserDialog.OnUserDialogListener() {
                @Override
                public void onUserSaved(User user, boolean isEdit) {
                    if (isEdit) {
                        updateUser(user);
                    } else {
                        saveUser(user);
                    }
                }
            });
        dialog.show();
    }

    private void showEditUserDialog(User user) {
        AddEditUserDialog dialog = new AddEditUserDialog(getContext(), user, 
            new AddEditUserDialog.OnUserDialogListener() {
                @Override
                public void onUserSaved(User user, boolean isEdit) {
                    updateUser(user);
                }
            });
        dialog.show();
    }

    private void saveUser(User user) {
        userRepository.add(user, "user", new CallBackUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "User saved successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser(User user) {
        userRepository.update(user, user.getId().getId(),  new CallBackUtil.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error updating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete \"" + user.getFull_name() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                userRepository.delete(user.getId().getId(), new CallBackUtil.OnDeleteCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onUserClick(User user) {
        // Show user details dialog
        new AlertDialog.Builder(getContext())
            .setTitle("User Details")
            .setMessage("Name: " + user.getFull_name() + "\n" +
                       "Email: " + user.getEmail() + "\n" +
                       "Phone: " + user.getPhone_number() + "\n" +
                       "Address: " + user.getAddress())
            .setPositiveButton("Edit", (dialog, which) -> showEditUserDialog(user))
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onUserEdit(User user) {
        showEditUserDialog(user);
    }

    @Override
    public void onUserDelete(User user) {
        deleteUser(user);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
        }
        realtimeUtil.removeAllListeners();
    }
} 