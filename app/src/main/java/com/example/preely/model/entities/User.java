package com.example.preely.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties
public class User extends BaseEntity {
    String full_name;
    String username;
    String phone_number;
    String encode_password;
    String address;
    String email;
    String province;
    String ward;
    boolean is_active;
    float rating;
    GeoPoint location;
    Timestamp last_login;
    Timestamp registration_date;
}
