package com.example.preely.model.entities;

import com.google.firebase.firestore.DocumentReference;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {
    DocumentReference service_id;
    DocumentReference reviewer_id;

    String comment;
    float rating;
}
