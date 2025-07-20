package com.example.preely.model.response;

import com.example.preely.model.dto.CommonDto;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;

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
public class UserResponse extends CommonResponse {

    String full_name;
    String username;
    String phone_number;
    String address;
    String email;
    String province;
    String ward;
    boolean is_active;
    float rating;
    GeoPoint location;

}
