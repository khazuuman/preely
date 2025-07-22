package com.example.preely.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;

import com.example.preely.model.entities.Message;
import com.example.preely.model.entities.User;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.Constraints;
import com.example.preely.util.RealtimeDbUtil;
import com.example.preely.util.NotificationUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.example.preely.util.Constraints.*;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class ChatViewModel extends ViewModel {
    private MainRepository<Message> messageRepo = new MainRepository<>(Message.class, CollectionName.IMAGE);
    private MainRepository<User> userRepo = new MainRepository<>(User.class, CollectionName.USERS);
    private MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<User> receiverUser = new MutableLiveData<>();
    private RealtimeDbUtil rtDbUtil = new RealtimeDbUtil();

    public void initChat(String receiverId, String senderId) {
        String room = getRoomId(senderId, receiverId);

        rtDbUtil.listenForMessages(room, message -> {
            List<Message> current = messages.getValue();
            current.add(message);
            messages.setValue(current);
            messageRepo.add(message, Constraints.CollectionName.MESSAGES, null);
        });

        Query query = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.MESSAGES)
                .whereEqualTo("room", room)
                .orderBy("send_at", Query.Direction.ASCENDING);
        messageRepo.getAll(query).observeForever(list -> messages.setValue(list));

        Query userQuery = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.USERS)
                .whereEqualTo("id", receiverId);
        userRepo.getOne(userQuery).observeForever(receiverUser::setValue);
    }

    public void sendMessage(String content, String senderId, String receiverId) {
        String room = getRoomId(senderId, receiverId);
        Message msg = new Message();
        msg.setContent(content);
        msg.setSender_id(senderId);
        msg.setReceiver_id(receiverId);
        msg.set_read(false);
        msg.setSend_at(new Timestamp(new java.util.Date()));
        msg.setRoom(room);
        rtDbUtil.sendMessageToRoom(room, msg);
    }

    private String getRoomId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    private void triggerNotification(Context context, Message msg) {
        Query unreadQuery = FirebaseFirestore.getInstance().collection(Constraints.CollectionName.NOTIFICATIONS)
                .whereEqualTo("user_id", msg.getReceiver_id())
                .whereEqualTo("is_read", false);
        messageRepo.getCount(unreadQuery).observeForever(count -> {
            NotificationUtil.applyDotBadge(context, count);  // Dot
            NotificationUtil.showNotification(context, "New Message", msg.getContent(), msg.getSender_id());  // Panel + Lock Screen
        });
    }
    public LiveData<List<Message>> getMessages() { return messages; }
    public LiveData<User> getReceiverUser() { return receiverUser; }


}

