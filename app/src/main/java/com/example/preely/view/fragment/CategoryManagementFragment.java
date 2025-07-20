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
import com.example.preely.adapter.CategoryAdapter;
import com.example.preely.dialog.AddEditCategoryDialog;
import com.example.preely.model.entities.Category;
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

public class CategoryManagementFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<Category> categoryList = new ArrayList<>();
    private List<Category> originalCategoryList = new ArrayList<>();
    private MainRepository<Category> categoryRepository;
    private CategoryAdapter categoryAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration categoryListener;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadCategories(); // Load data first
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
        recyclerView = view.findViewById(R.id.recycler_categories);
        fabAdd = view.findViewById(R.id.fab_add_category);
        etSearch = view.findViewById(R.id.et_search_categories);
        categoryRepository = new MainRepository<>(Category.class);
        realtimeUtil = new FirestoreRealtimeUtil();
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setOnCategoryClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(categoryAdapter);
    }

    private void setupSearch() {
        SearchFilterUtil.setupCategorySearch(etSearch, originalCategoryList, categoryAdapter, 
            new SearchFilterUtil.SearchFilterCallback<Category>() {
                @Override
                public void onFiltered(List<Category> filteredList) {
                    categoryList.clear();
                    categoryList.addAll(filteredList);
                }
            });
    }

    private void setupRealtimeListener() {
        categoryListener = realtimeUtil.listenToCategories(new FirestoreRealtimeUtil.RealtimeListener<Category>() {
            @Override
            public void onDataAdded(Category category) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Check if category already exists to avoid duplicate notifications
                        boolean categoryExists = originalCategoryList.stream()
                            .anyMatch(existingCategory -> existingCategory.getId().equals(category.getId()));
                        
                        if (!categoryExists) {
                            originalCategoryList.add(category);
                            categoryList.add(category);
                            categoryAdapter.setCategoryList(categoryList);
                            Toast.makeText(getContext(), "New category added: " + category.getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onDataModified(Category category) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateCategoryInList(originalCategoryList, category);
                        updateCategoryInList(categoryList, category);
                        categoryAdapter.setCategoryList(categoryList);
                        Toast.makeText(getContext(), "Category updated: " + category.getName(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onDataRemoved(Category category) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        removeCategoryFromList(originalCategoryList, category);
                        removeCategoryFromList(categoryList, category);
                        categoryAdapter.setCategoryList(categoryList);
                        Toast.makeText(getContext(), "Category removed: " + category.getName(), Toast.LENGTH_SHORT).show();
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

    private void updateCategoryInList(List<Category> list, Category updatedCategory) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedCategory.getId())) {
                list.set(i, updatedCategory);
                break;
            }
        }
    }

    private void removeCategoryFromList(List<Category> list, Category categoryToRemove) {
        list.removeIf(category -> category.getId().equals(categoryToRemove.getId()));
    }

    private void loadCategories() {
        Query query = db.collection("category");
        categoryRepository.getAll(query).observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                originalCategoryList.clear();
                categoryList.clear();
                originalCategoryList.addAll(categories);
                categoryList.addAll(categories);
                categoryAdapter.setCategoryList(categoryList);
            }
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddCategoryDialog();
        });
    }

    private void showAddCategoryDialog() {
        AddEditCategoryDialog dialog = new AddEditCategoryDialog(getContext(), null, 
            new AddEditCategoryDialog.OnCategoryDialogListener() {
                @Override
                public void onCategorySaved(Category category, boolean isEdit) {
                    if (isEdit) {
                        updateCategory(category);
                    } else {
                        saveCategory(category);
                    }
                }
            });
        dialog.show();
    }

    private void showEditCategoryDialog(Category category) {
        AddEditCategoryDialog dialog = new AddEditCategoryDialog(getContext(), category, 
            new AddEditCategoryDialog.OnCategoryDialogListener() {
                @Override
                public void onCategorySaved(Category category, boolean isEdit) {
                    updateCategory(category);
                }
            });
        dialog.show();
    }

    private void saveCategory(Category category) {
        categoryRepository.add(category, "category", new DbUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "Category saved successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error saving category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategory(Category category) {
        categoryRepository.update(category, category.getId(), "category", new DbUtil.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Category updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error updating category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCategory(Category category) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete \"" + category.getName() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                categoryRepository.delete(category.getId(), "category", new DbUtil.OnDeleteCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onCategoryClick(Category category) {
        // Show category details dialog
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(category.getName()).append("\n");
        if (category.getParent_category_id() != null) {
            details.append("Parent Category ID: ").append(category.getParent_category_id());
        } else {
            details.append("Parent Category: Root Category");
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Category Details")
            .setMessage(details.toString())
            .setPositiveButton("Edit", (dialog, which) -> showEditCategoryDialog(category))
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onCategoryEdit(Category category) {
        showEditCategoryDialog(category);
    }

    @Override
    public void onCategoryDelete(Category category) {
        deleteCategory(category);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (categoryListener != null) {
            categoryListener.remove();
        }
        realtimeUtil.removeAllListeners();
    }
} 