package com.example.preely.model.request;

import com.google.firebase.firestore.DocumentReference;

import lombok.Getter;
import lombok.Setter;

@Setter
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

}