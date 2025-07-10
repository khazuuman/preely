package com.example.preely.model.entities;

import com.google.firebase.firestore.DocumentId;

import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post extends BaseEntity{

    String category_id;
    String seller_id;
    BigDecimal price;
    String status;
    String title;
    String currency;
    String description;
    String location;
    BigInteger view_count;
    String ward;
    String province;

}
