package com.example.preely.model.response;

import com.google.firebase.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    String id;
    String senderId;
    String senderName;
    String receiverId;
    String postId;
    String content;
    boolean isRead;
    Timestamp sendAt;
    String room;
}
