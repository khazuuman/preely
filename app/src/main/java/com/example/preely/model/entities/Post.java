package com.example.preely.model.entities;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

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
@IgnoreExtraProperties
public class Post extends BaseEntity implements java.io.Serializable {

    DocumentReference category_id;
    DocumentReference seller_id;
    
    Double price;
    String status;
    String title;
    String currency;
    String description;
    GeoPoint location;
    Long views_count;
    List<String> images;
    String ward;
    String province;
    List<String> tag_ids;

//    // Helper methods to extract IDs from DocumentReference
//    @Exclude
//    public String getCategoryId() {
//        return category_id != null ? category_id.getId() : null;
//    }
//
//    @Exclude
//    public String getSellerId() {
//        return seller_id != null ? seller_id.getId() : null;
//    }
//
//    @Exclude
//    public String getLocationString() {
//        if (location != null) {
//            return location.getLatitude() + "," + location.getLongitude();
//        }
//        return null;
//    }
}
