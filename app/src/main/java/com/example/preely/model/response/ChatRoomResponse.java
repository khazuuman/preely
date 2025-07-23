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
    private String receiverName;
    private String lastMessage;
    private Timestamp lastSendAt;
}
