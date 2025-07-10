package com.example.preely.model;

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
public class Message {

    @DocumentId
    String id;
    String sender_id;
    String receiver_id;
    String post_id;
    String content;
    boolean is_read;
    String send_at;

}
