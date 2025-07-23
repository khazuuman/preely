package com.example.preely.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference; // Import thêm để hỗ trợ reference

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message extends BaseEntity {
    DocumentReference sender_id;
    DocumentReference receiver_id;
    DocumentReference post_id;
    String content;
    boolean is_read;
    Timestamp send_at;
    String room;
}
