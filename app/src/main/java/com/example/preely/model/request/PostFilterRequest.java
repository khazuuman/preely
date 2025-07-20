package com.example.preely.model.request;

import com.google.firebase.firestore.DocumentReference;

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
public class PostFilterRequest {

    List<String> category_id;
    List<String> status;
    String ward;
    String province;
    List<String> tag_id;

}
