package com.example.preely.model.entities;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.math.BigDecimal;
import java.math.BigInteger;
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
public class Post extends BaseEntity {

    DocumentReference category_id;
    DocumentReference seller_id;
    Double price;
    String status;
    String title;
    String currency;
    String description;
    GeoPoint location;
    Long view_count;
    String ward;
    String province;
    List<String> tag_ids;

}
