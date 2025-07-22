package com.example.preely.model.response;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
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
public class PostResponse extends CommonResponse implements Serializable {

    DocumentReference seller_id;
    Double price;
    String status;
    String title;
    String currency;
    String description;
    GeoPoint location;
    Long views_count;
    String ward;
    String province;
    CategoryResponse categoryResponse;
    List<String> image;
    List<TagResponse> tagResponses;

    @Override
    public String toString() {
        return "{ view_count=" + views_count + ", title=" + title + "}";
    }

}
