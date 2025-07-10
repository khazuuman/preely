package com.example.preely.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @DocumentId
    String id;
    String name;
    String parent_category_id;
    Timestamp create_at;
    Timestamp update_at;

}
