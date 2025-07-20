package com.example.preely.model.request;

import com.google.firebase.firestore.DocumentReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagFilterRequest {

    DocumentReference id;
    String name;
    boolean isChecked;

}
