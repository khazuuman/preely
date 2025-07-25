package com.example.preely.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.PropertyName;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {
    DocumentReference sender_id;
    DocumentReference receiver_id;
    DocumentReference service_id;

    String content;

    @PropertyName("_read")
    boolean is_read;

    Timestamp send_at;
    String room;
}
