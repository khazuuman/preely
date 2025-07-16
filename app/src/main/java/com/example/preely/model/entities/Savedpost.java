package com.example.preely.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Savedpost extends BaseEntity{

    String post_id;
    Timestamp save_date;
    String user_id;

}
