package com.example.preely.model.dto;

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
public class MessageDto {
    String id;
    String sender_id;
    String sender_full_name;   // Thêm để mapping từ User entity
    String sender_username;    // Thêm để mapping từ User entity
    String receiver_id;
    String receiver_full_name; // Thêm để mapping từ User entity
    String receiver_username;  // Thêm để mapping từ User entity
    String service_id;
    String content;
    boolean is_read;
    Timestamp send_at;
    String room;
}
