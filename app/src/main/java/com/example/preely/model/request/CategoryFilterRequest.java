package com.example.preely.model.request;

import com.google.firebase.firestore.DocumentReference;

import lombok.Getter;

@Getter
public class CategoryFilterRequest {
    private String id;
    private String name;
    private boolean checked;

    public CategoryFilterRequest() {}
    public CategoryFilterRequest(String id, String name, boolean checked) {
        this.id = id;
        this.name = name;
        this.checked = checked;
    }

    public void setId(String id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setChecked(boolean checked) { this.checked = checked; }
} 