package com.example.preely.model;

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
public class Post {

    @DocumentId
    String id;
    String category_id;
    String seller_id;
    BigDecimal price;
    String status;
    String title;
    String currency;
    String description;
    String location;
    String create_at;
    String update_at;
    BigInteger view_count;
    String ward;
    String province;

}
