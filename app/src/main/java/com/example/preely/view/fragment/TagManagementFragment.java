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
import com.example.preely.adapter.TagAdapter;
import com.example.preely.dialog.AddEditTagDialog;
import com.example.preely.model.entities.Tag;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.example.preely.util.PaginationUtil;
import com.example.preely.util.SearchFilterUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import com.example.preely.util.DbUtil;
import com.example.preely.util.Constraints.*;
import java.util.ArrayList;
import java.util.List;

public class TagManagementFragment extends Fragment implements TagAdapter.OnTagClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private List<Tag> tagList = new ArrayList<>();
    private List<Tag> originalTagList = new ArrayList<>();
    private MainRepository<Tag> tagRepository;
    private TagAdapter tagAdapter;
    private FirestoreRealtimeUtil realtimeUtil;
    private ListenerRegistration tagListener;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadTags(); // Load data first
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
        recyclerView = view.findViewById(R.id.recycler_tags);
        fabAdd = view.findViewById(R.id.fab_add_tag);
        etSearch = view.findViewById(R.id.et_search_tags);
        tagRepository = new MainRepository<>(Tag.class, CollectionName.TAGS);
        realtimeUtil = new FirestoreRealtimeUtil();
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        tagAdapter = new TagAdapter();
        tagAdapter.setOnTagClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(tagAdapter);
    }

    private void setupSearch() {
        SearchFilterUtil.setupTagSearch(etSearch, originalTagList, tagAdapter, 
            new SearchFilterUtil.SearchFilterCallback<Tag>() {
                @Override
                public void onFiltered(List<Tag> filteredList) {
                    tagList.clear();
                    tagList.addAll(filteredList);
                }
            });
    }

    private void setupRealtimeListener() {
        tagListener = realtimeUtil.listenToTags(new FirestoreRealtimeUtil.RealtimeListener<Tag>() {
            @Override
            public void onDataAdded(Tag tag) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Check if tag already exists to avoid duplicate notifications
                        boolean tagExists = originalTagList.stream()
                            .anyMatch(existingTag -> existingTag.getId().equals(tag.getId()));
                        
                        if (!tagExists) {
                            originalTagList.add(tag);
                            tagList.add(tag);
                            tagAdapter.setTagList(tagList);
                            Toast.makeText(getContext(), "New tag added: " + tag.getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onDataModified(Tag tag) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateTagInList(originalTagList, tag);
                        updateTagInList(tagList, tag);
                        tagAdapter.setTagList(tagList);
                        Toast.makeText(getContext(), "Tag updated: " + tag.getName(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onDataRemoved(Tag tag) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        removeTagFromList(originalTagList, tag);
                        removeTagFromList(tagList, tag);
                        tagAdapter.setTagList(tagList);
                        Toast.makeText(getContext(), "Tag removed: " + tag.getName(), Toast.LENGTH_SHORT).show();
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

    private void updateTagInList(List<Tag> list, Tag updatedTag) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedTag.getId())) {
                list.set(i, updatedTag);
                break;
            }
        }
    }

    private void removeTagFromList(List<Tag> list, Tag tagToRemove) {
        list.removeIf(tag -> tag.getId().equals(tagToRemove.getId()));
    }

    private void loadTags() {
        Query query = db.collection("tag");
        tagRepository.getAll(query).observe(getViewLifecycleOwner(), tags -> {
            if (tags != null) {
                originalTagList.clear();
                tagList.clear();
                originalTagList.addAll(tags);
                tagList.addAll(tags);
                tagAdapter.setTagList(tagList);
            }
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            showAddTagDialog();
        });
    }

    private void showAddTagDialog() {
        AddEditTagDialog dialog = new AddEditTagDialog(getContext(), null, 
            new AddEditTagDialog.OnTagDialogListener() {
                @Override
                public void onTagSaved(Tag tag, boolean isEdit) {
                    if (isEdit) {
                        updateTag(tag);
                    } else {
                        saveTag(tag);
                    }
                }
            });
        dialog.show();
    }

    private void showEditTagDialog(Tag tag) {
        AddEditTagDialog dialog = new AddEditTagDialog(getContext(), tag, 
            new AddEditTagDialog.OnTagDialogListener() {
                @Override
                public void onTagSaved(Tag tag, boolean isEdit) {
                    updateTag(tag);
                }
            });
        dialog.show();
    }

    private void saveTag(Tag tag) {
        tagRepository.add(tag, "tag", new CallBackUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "Tag saved successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error saving tag: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTag(Tag tag) {
        tagRepository.update(tag, tag.getId().getId(), new CallBackUtil.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Tag updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error updating tag: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTag(Tag tag) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Tag")
            .setMessage("Are you sure you want to delete \"" + tag.getName() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                tagRepository.delete(tag.getId().getId(), new CallBackUtil.OnDeleteCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Tag deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting tag: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onTagClick(Tag tag) {
        // Show tag details dialog
        new AlertDialog.Builder(getContext())
            .setTitle("Tag Details")
            .setMessage("Name: " + tag.getName())
            .setPositiveButton("Edit", (dialog, which) -> showEditTagDialog(tag))
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onTagEdit(Tag tag) {
        showEditTagDialog(tag);
    }

    @Override
    public void onTagDelete(Tag tag) {
        deleteTag(tag);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tagListener != null) {
            tagListener.remove();
        }
        realtimeUtil.removeAllListeners();
    }
} 