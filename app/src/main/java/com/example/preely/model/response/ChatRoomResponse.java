package com.example.preely.model.response;

import com.google.firebase.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponse {
    private String roomId;
    private String receiverId;
    private String receiverFullName;  // Hiển thị chính trên item_chat_room
    private String receiverUsername;  // Hiển thị phụ và dùng cho search
    private String lastMessage;
    private Timestamp lastSendAt;

}
