package com.example.preely.model.request;

import com.google.firebase.firestore.DocumentReference;

public class CategoryFilterRequest {
    private DocumentReference id;
    private String name;
    private boolean checked;

    public CategoryFilterRequest() {}
    public CategoryFilterRequest(DocumentReference id, String name, boolean checked) {
        this.id = id;
        this.name = name;
        this.checked = checked;
    }
    public DocumentReference getId() { return id; }
    public void setId(DocumentReference id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
} 