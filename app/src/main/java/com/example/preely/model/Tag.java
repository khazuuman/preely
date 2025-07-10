package com.example.preely.model;

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
public class Tag {

    @DocumentId
    String id;
    String name;
    String create_at;
    String update_at;

}
