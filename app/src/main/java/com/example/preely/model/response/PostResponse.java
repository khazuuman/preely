package com.example.preely.model.response;

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
public class PostResponse extends CommonResponse {

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
    CategoryResponse categoryResponse;
    List<TagResponse> tagResponses;

    @Override
    public String toString() {
        return "PostResponse{" + super.toString() + ", seller_id=" + seller_id + ", price=" + price + ", status=" + status + ", title=" + title + ", currency=" + currency + ", description=" + description + ", location=" + location + ", view_count=" + view_count + '}';
    }

}
