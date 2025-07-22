package com.example.preely.model.request;

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
public class CreateMessageRequest {
    String senderId;
    String receiverId;
    String postId;
    String content;
    boolean isRead = false;
    String room;
    Timestamp sendAt;
}
