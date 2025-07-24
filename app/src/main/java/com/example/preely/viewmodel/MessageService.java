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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageService extends ViewModel {
    private MainRepository<Message> repository = new MainRepository<>(Message.class, Constraints.CollectionName.MESSAGES);
    private MutableLiveData<List<ChatRoomResponse>> chatRoomsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Message>> messagesForRoomLiveData = new MutableLiveData<>();
    private UserService userService = new UserService();

    // Interfaces
    public interface SendMessageCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface MarkAsReadCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // LiveData getters
    public LiveData<List<ChatRoomResponse>> getChatRoomsLiveData() {
        return chatRoomsLiveData;
    }

    public LiveData<List<Message>> getMessagesForRoom(String roomId) {
        return messagesForRoomLiveData;
    }

    /**
     * Lấy danh sách chat rooms cho user hiện tại
     * Sử dụng UserService.getUserInfo để lấy cả fullName và username
     */
    public void getChatRooms(DocumentReference currentUserRef) {
        // Query tin nhắn mà user là sender
        Query senderQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("sender_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        // Query tin nhắn mà user là receiver
        Query receiverQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("receiver_id", currentUserRef)
                .orderBy("send_at", Query.Direction.DESCENDING);

        repository.getList(senderQuery, null, null).observeForever(senderMessages -> {
            repository.getList(receiverQuery, null, null).observeForever(receiverMessages -> {
                List<Message> allMessages = new ArrayList<>();

                if (senderMessages != null) allMessages.addAll(senderMessages);
                if (receiverMessages != null) allMessages.addAll(receiverMessages);

                Log.d("MessageService", "Total messages fetched: " + allMessages.size());

                if (!allMessages.isEmpty()) {
                    // Sắp xếp tin nhắn theo thời gian mới nhất
                    allMessages.sort(Comparator.comparing(Message::getSend_at).reversed());

                    Map<String, ChatRoomResponse> roomMap = new HashMap<>();
                    final int[] pendingUserFetches = {0};

                    for (Message msg : allMessages) {
                        String room = msg.getRoom();
                        if (room != null && !roomMap.containsKey(room)) {
                            String senderIdStr = msg.getSender_id() != null ? msg.getSender_id().getId() : null;
                            String receiverIdStr = msg.getReceiver_id() != null ? msg.getReceiver_id().getId() : null;

                            // Xác định ai là người chat với mình
                            String otherUserId = (senderIdStr != null && senderIdStr.equals(currentUserRef.getId()))
                                    ? receiverIdStr : senderIdStr;

                            if (otherUserId != null) {
                                ChatRoomResponse response = new ChatRoomResponse(
                                        room,
                                        otherUserId,
                                        "Loading...",  // Tạm thời
                                        "Loading...",  // Tạm thời
                                        msg.getContent(),
                                        msg.getSend_at()
                                );
                                roomMap.put(room, response);

                                // Fetch thông tin user (cả fullName và username)
                                pendingUserFetches[0]++;
                                userService.getUserInfo(otherUserId, new UserService.UserInfoCallback() {
                                    @Override
                                    public void onSuccess(String fullName, String username) {
                                        response.setReceiverFullName(fullName);
                                        response.setReceiverUsername(username);
                                        pendingUserFetches[0]--;

                                        Log.d("MessageService", "User info updated - Full name: " + fullName + ", Username: " + username + " for room: " + room);

                                        // Nếu đã fetch xong tất cả, cập nhật LiveData
                                        if (pendingUserFetches[0] == 0) {
                                            chatRoomsLiveData.setValue(new ArrayList<>(roomMap.values()));
                                            Log.d("MessageService", "All user info fetched, updating chat rooms: " + roomMap.size());
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        // Set default values khi không lấy được thông tin
                                        response.setReceiverFullName("Unknown User");
                                        response.setReceiverUsername("unknown");
                                        pendingUserFetches[0]--;

                                        Log.e("MessageService", "Failed to fetch user info for " + otherUserId + ": " + error);

                                        // Vẫn cập nhật LiveData dù có lỗi
                                        if (pendingUserFetches[0] == 0) {
                                            chatRoomsLiveData.setValue(new ArrayList<>(roomMap.values()));
                                        }
                                    }
                                });
                            }
                        }
                    }

                    // Trường hợp không cần fetch user info
                    if (pendingUserFetches[0] == 0) {
                        chatRoomsLiveData.setValue(new ArrayList<>(roomMap.values()));
                    }

                } else {
                    // Không có tin nhắn nào
                    chatRoomsLiveData.setValue(new ArrayList<>());
                    Log.d("MessageService", "No messages found for user");
                }
            });
        });
    }

    /**
     * Load tin nhắn cho một room cụ thể
     */
    public void loadMessagesForRoom(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            messagesForRoomLiveData.setValue(new ArrayList<>());
            return;
        }

        Query query = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("room", roomId)
                .orderBy("send_at", Query.Direction.ASCENDING);

        repository.getList(query, null, null).observeForever(messages -> {
            if (messages != null) {
                messagesForRoomLiveData.setValue(messages);
                Log.d("MessageService", "Loaded " + messages.size() + " messages for room: " + roomId);
            } else {
                messagesForRoomLiveData.setValue(new ArrayList<>());
            }
        });
    }

    /**
     * Gửi tin nhắn mới
     */
    public void sendMessage(CreateMessageRequest request, SendMessageCallback callback) {
        // Chuyển đổi request thành entity
        Message message = new Message();

        DocumentReference senderRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .document(request.getSenderId());
        DocumentReference receiverRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .document(request.getReceiverId());

        message.setSender_id(senderRef);
        message.setReceiver_id(receiverRef);

        // Set service_id nếu có
        if (request.getServiceId() != null && !request.getServiceId().isEmpty()) {
            DocumentReference serviceRef = FirebaseFirestore.getInstance()
                    .collection("services")  // Hoặc collection name phù hợp
                    .document(request.getServiceId());
            message.setService_id(serviceRef);
        }

        message.setContent(request.getContent());
        message.setRoom(request.getRoom());
        message.setSend_at(request.getSendAt());
        message.set_read(request.isRead());

        // Sử dụng MainRepository để add
        repository.add(message, Constraints.CollectionName.MESSAGES, new CallBackUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("MessageService", "Message sent successfully with ID: " + documentReference.getId());
                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MessageService", "Failed to send message: " + e.getMessage());
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    /**
     * Đánh dấu tin nhắn đã đọc cho toàn bộ room
     */
    public void markRoomMessagesAsRead(String roomId, String currentUserId, MarkAsReadCallback callback) {
        DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.USERS)
                .document(currentUserId);

        Query unreadInRoomQuery = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("room", roomId)
                .whereEqualTo("receiver_id", currentUserRef)
                .whereEqualTo("is_read", false);

        unreadInRoomQuery.get().addOnSuccessListener(querySnapshot -> {
            if (querySnapshot.isEmpty()) {
                if (callback != null) callback.onSuccess();
                return;
            }

            WriteBatch batch = FirebaseFirestore.getInstance().batch();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                batch.update(doc.getReference(), "is_read", true);
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("MessageService", "Marked " + querySnapshot.size() + " messages as read in room: " + roomId);
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MessageService", "Failed to mark messages as read: " + e.getMessage());
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
        }).addOnFailureListener(e -> {
            Log.e("MessageService", "Failed to query unread messages: " + e.getMessage());
            if (callback != null) callback.onFailure(e.getMessage());
        });
    }
}
