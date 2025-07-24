package com.example.preely.model.request;

import com.google.firebase.firestore.DocumentReference;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceFilterRequest {
    String title;
    List<String> category_ids;
    Integer sortType;
    Float rating;
    List<String> availability;

} 