package com.example.preely.model.entities;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service extends BaseEntity {
    DocumentReference category_id;
    DocumentReference provider_id;


    String title;
    String description;
    Double price;
    String availability; //"Weekends", "Mon-Fri evenings"
    String university;
    Float average_rating;
    Integer total_reviews;
    String status;
    List<String> image_urls;
    GeoPoint location;
}
