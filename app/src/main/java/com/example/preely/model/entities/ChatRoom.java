package com.example.preely.model.entities;

import com.google.firebase.firestore.DocumentReference;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseEntity {
    String room_id;
    DocumentReference service_id;
    DocumentReference seeker_id;
    DocumentReference provider_id;
}
