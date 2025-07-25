package com.example.preely.model.response;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMarketDetailResponse extends CommonResponse {
    String title;
    String providerName;
    String categoryName;
    String university;
    String availability;
    Double price;
    String status;
    String description;
    Float average_rating;
    Integer total_reviews;
    List<String> image_urls;
    GeoPoint location;
    Timestamp create_at;
    Timestamp update_at;
}
