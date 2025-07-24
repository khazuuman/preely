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
    String senderFullName;      // Mapping từ User entity khi trả về
    String senderUsername;      // Mapping từ User entity khi trả về
    String receiverId;
    String receiverFullName;    // Mapping từ User entity khi trả về
    String receiverUsername;    // Mapping từ User entity khi trả về
    String serviceId;
    String content;
    boolean isRead;
    Timestamp sendAt;
    String room;
}
