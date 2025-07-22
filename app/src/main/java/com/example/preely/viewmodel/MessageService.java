package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Message;
import com.example.preely.model.request.CreateMessageRequest;
import com.example.preely.model.response.ChatRoomResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.Constraints;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageService extends ViewModel {
    private MainRepository<Message> repository = new MainRepository<>(Message.class, Constraints.CollectionName.MESSAGES);
    private MutableLiveData<List<ChatRoomResponse>> chatRoomsLiveData = new MutableLiveData<>();
    private UserService userService = new UserService();
    public LiveData<List<ChatRoomResponse>> getChatRoomsLiveData() {
        return chatRoomsLiveData;
    }

    public void getChatRooms(DocumentReference currentUserRef) {
        // Query sender
        Query senderQuery = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("sender_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        // Query receiver
        Query receiverQuery = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("receiver_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        repository.getList(senderQuery, null, null).observeForever(senderMessages -> {
            if (senderMessages == null) Log.e("DEBUG", "Sender query failed");
            repository.getList(receiverQuery, null, null).observeForever(receiverMessages -> {
                if (receiverMessages == null) Log.e("DEBUG", "Receiver query failed");
                List<Message> allMessages = new ArrayList<>();
                if (senderMessages != null) allMessages.addAll(senderMessages);
                if (receiverMessages != null) allMessages.addAll(receiverMessages);

                Log.d("DEBUG", "Total messages fetched: " + allMessages.size());

                if (!allMessages.isEmpty()) {
                    allMessages.sort(Comparator.comparing(Message::getSend_at).reversed());

                    Map<String, ChatRoomResponse> roomMap = new HashMap<>();

                    final int[] pendingUserFetches = {0};
                    final int totalRooms = (int) allMessages.stream()
                            .map(Message::getRoom)
                            .distinct()
                            .count();

                    for (Message msg : allMessages) {
                        String room = msg.getRoom();
                        if (!roomMap.containsKey(room)) {
                            String senderIdStr = msg.getSender_id() != null ? msg.getSender_id().getId() : null;
                            String receiverIdStr = msg.getReceiver_id() != null ? msg.getReceiver_id().getId() : null;
                            String receiverId = (senderIdStr != null && senderIdStr.equals(currentUserRef.getId())) ? receiverIdStr : senderIdStr;

                            Log.d("DEBUG", "Message: sender=" + senderIdStr + ", receiver=" + receiverIdStr + ", room=" + room);

                            ChatRoomResponse response = new ChatRoomResponse(
                                    room,
                                    receiverId,
                                    "Loading...", // Placeholder tạm thời
                                    msg.getContent(),
                                    msg.getSend_at()
                            );
                            roomMap.put(room, response);

                            pendingUserFetches[0]++;
                            userService.getUserName(receiverId, new UserService.UserNameCallback() {
                                @Override
                                public void onSuccess(String userName) {
                                    // Cập nhật tên thật vào response
                                    response.setReceiverName(userName);
                                    pendingUserFetches[0]--;

                                    Log.d("DEBUG", "User name updated: " + userName + " for room: " + room);

                                    // Nếu đã fetch xong tất cả user names, update LiveData
                                    if (pendingUserFetches[0] == 0) {
                                        chatRoomsLiveData.setValue(new ArrayList<>(roomMap.values()));
                                        Log.d("DEBUG", "All user names fetched, updating chat rooms: " + roomMap.size());
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    // Giữ tên placeholder hoặc set default
                                    response.setReceiverName("Unknown User");
                                    pendingUserFetches[0]--;

                                    Log.e("DEBUG", "Failed to fetch user name for " + receiverId + ": " + error);

                                    // Vẫn update LiveData dù có lỗi
                                    if (pendingUserFetches[0] == 0) {
                                        chatRoomsLiveData.setValue(new ArrayList<>(roomMap.values()));
                                    }
                                }
                            });
                        }
                    }

                    if (pendingUserFetches[0] == 0) {
                        chatRoomsLiveData.setValue(new ArrayList<>(roomMap.values()));
                    }

                } else {
                    chatRoomsLiveData.setValue(null);
                    Log.d("DEBUG", "No messages found for user");
                }
            });
        });
    }

    // Thêm vào MessageService class
    private MutableLiveData<List<Message>> messagesForRoomLiveData = new MutableLiveData<>();

    public LiveData<List<Message>> getMessagesForRoom(String roomId) {
        return messagesForRoomLiveData;
    }

    public void loadMessagesForRoom(String roomId) {
        Query query = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("room", roomId)
                .orderBy("send_at", Query.Direction.ASCENDING);

        repository.getList(query, null, null).observeForever(messages -> {
            if (messages != null) {
                messagesForRoomLiveData.setValue(messages);
                Log.d("DEBUG", "Loaded " + messages.size() + " messages for room: " + roomId);
            } else {
                messagesForRoomLiveData.setValue(new ArrayList<>());
            }
        });
    }

    // Interface cho callback gửi tin nhắn
    public interface SendMessageCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void sendMessage(CreateMessageRequest request, SendMessageCallback callback) {
        // Convert request to entity
        Message message = new Message();

        DocumentReference senderRef = FirebaseFirestore.getInstance()
                .collection("user").document(request.getSenderId());
        DocumentReference receiverRef = FirebaseFirestore.getInstance()
                .collection("user").document(request.getReceiverId());

        message.setSender_id(senderRef);
        message.setReceiver_id(receiverRef);
        message.setContent(request.getContent());
        message.setRoom(request.getRoom());
        message.setSend_at(request.getSendAt());
        message.set_read(request.isRead());

        // Sử dụng MainRepository để add
        repository.add(message, Constraints.CollectionName.MESSAGES, new CallBackUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    public interface MarkAsReadCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void markMessageAsRead(String messageId, MarkAsReadCallback callback) {
        DocumentReference messageRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .document(messageId);

        messageRef.update("is_read", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MessageService", "Message marked as read: " + messageId);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("MessageService", "Failed to mark message as read", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void markRoomMessagesAsRead(String roomId, String currentUserId, MarkAsReadCallback callback) {
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection("user").document(currentUserId);

        Query unreadInRoomQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("room", roomId)
                .whereEqualTo("receiver_id", currentUserRef)
                .whereEqualTo("is_read", false);

        unreadInRoomQuery.get().addOnSuccessListener(querySnapshot -> {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                batch.update(doc.getReference(), "is_read", true);
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("MessageService", "Marked " + querySnapshot.size() + " messages as read");
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
        });
    }

}
