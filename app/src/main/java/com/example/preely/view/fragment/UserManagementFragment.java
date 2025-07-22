package com.example.preely.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;

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
import com.example.preely.viewmodel.ManagementUserService;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<User> userList = new ArrayList<>();
    private List<User> originalUserList = new ArrayList<>();
    private List<User> allUserList = new ArrayList<>(); // Lưu toàn bộ user
    private List<User> pagedUserList = new ArrayList<>(); // Danh sách hiển thị theo trang
    private ManagementUserService userService;
    private UserAdapter userAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration userListener;
    private FirebaseFirestore db;
    private boolean isInitialLoad = true;
    private AddEditUserDialog addEditUserDialog; // Lưu instance dialog
    private ActivityResultLauncher<Intent> avatarPickerLauncher;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avatarPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (addEditUserDialog != null && result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    addEditUserDialog.onActivityResult(1001, result.getResultCode(), result.getData());
                }
            }
        );
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_users);
        fabAdd = view.findViewById(R.id.fab_add_user);
        etSearch = view.findViewById(R.id.et_search_users);
        userService = new ManagementUserService();
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
        userService.listenRealtime(new FirestoreRealtimeUtil.RealtimeListener<User>() {
            @Override
            public void onDataAdded(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        boolean userExists = originalUserList.stream()
                            .anyMatch(existingUser -> existingUser.getId().equals(user.getId()));
                        if (!userExists) {
                            originalUserList.add(user);
                            userList.add(user);
                            userAdapter.setUserList(userList);
                            if (!isInitialLoad) {
                                Toast.makeText(getContext(), "New user added: " + user.getFull_name(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            @Override
            public void onDataModified(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
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
        recyclerView.postDelayed(() -> isInitialLoad = false, 1000);
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
        userService.getAllUsers(users -> {
            if (users != null) {
                allUserList.clear();
                pagedUserList.clear();
                allUserList.addAll(users);
                // Lấy trang đầu tiên
                pagedUserList.addAll(PaginationUtil.getPageItems(allUserList, 0));
                userAdapter.setUserList(pagedUserList);
                PaginationUtil.resetPagination();
                setupPagination();
            }
        });
    }

    private void setupPagination() {
        PaginationUtil.setupPagination(recyclerView, allUserList, new PaginationUtil.PaginationCallback<User>() {
            @Override
            public void onLoadMore(List<User> newItems, int page) {
                int oldSize = pagedUserList.size();
                pagedUserList.addAll(newItems);
                recyclerView.post(() -> userAdapter.notifyItemRangeInserted(oldSize, newItems.size()));
            }
            @Override
            public void onLoadComplete() {}
            @Override
            public void onError(String error) {}
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddUserDialog();
        });
    }

    private void showAddUserDialog() {
        addEditUserDialog = new AddEditUserDialog(getContext(), null, 
            new AddEditUserDialog.OnUserDialogListener() {
                @Override
                public void onUserSaved(User user, boolean isEdit) {
                    if (isEdit) {
                        updateUser(user);
                    } else {
                        saveUser(user);
                    }
                }
            },
            avatarPickerLauncher
        );
        addEditUserDialog.show();
    }

    private void showEditUserDialog(User user) {
        addEditUserDialog = new AddEditUserDialog(getContext(), user, 
            new AddEditUserDialog.OnUserDialogListener() {
                @Override
                public void onUserSaved(User user, boolean isEdit) {
                    updateUser(user);
                }
            },
            avatarPickerLauncher
        );
        addEditUserDialog.show();
    }

    private void saveUser(User user) {
        userService.addUser(user, new CallBackUtil.OnInsertCallback() {
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
        userService.updateUser(user, new CallBackUtil.OnUpdateCallback() {
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
                userService.deleteUser(user, new CallBackUtil.OnDeleteCallBack() {
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
        // Custom dialog layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 32);
        layout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        // Avatar
        ImageView avatar = new ImageView(getContext());
        int avatarSize = (int) (getResources().getDisplayMetrics().density * 120);
        android.widget.LinearLayout.LayoutParams avatarParams = new android.widget.LinearLayout.LayoutParams(avatarSize, avatarSize);
        avatarParams.bottomMargin = 32;
        avatar.setLayoutParams(avatarParams);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setBackgroundResource(R.drawable.rounded_edge);
        // Load avatar (nếu có), fallback icon
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(getContext()).load(user.getAvatar()).placeholder(R.drawable.ic_account).into(avatar);
        } else {
            avatar.setImageResource(R.drawable.ic_account);
        }
        layout.addView(avatar);

        // Tên
        android.widget.TextView nameTv = new android.widget.TextView(getContext());
        nameTv.setText(user.getFull_name() != null ? user.getFull_name() : "N/A");
        nameTv.setTextSize(22);
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        nameTv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layout.addView(nameTv);

        // Email
        android.widget.TextView emailTv = new android.widget.TextView(getContext());
        emailTv.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
        emailTv.setTextSize(16);
        emailTv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layout.addView(emailTv);

        // Phone
        android.widget.TextView phoneTv = new android.widget.TextView(getContext());
        phoneTv.setText("Phone: " + (user.getPhone_number() != null ? user.getPhone_number() : "N/A"));
        phoneTv.setTextSize(16);
        phoneTv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layout.addView(phoneTv);

        // Address
        android.widget.TextView addressTv = new android.widget.TextView(getContext());
        addressTv.setText("Address: " + (user.getAddress() != null ? user.getAddress() : "N/A"));
        addressTv.setTextSize(16);
        addressTv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layout.addView(addressTv);

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("User Details")
            .setView(layout)
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
        userService.removeRealtimeListener();
    }
} 