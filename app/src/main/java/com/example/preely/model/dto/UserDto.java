package com.example.preely.model.dto;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends CommonDto{
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
