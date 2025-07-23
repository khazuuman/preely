package com.example.preely.viewmodel;

import androidx.lifecycle.Observer;
import com.example.preely.model.entities.Tag;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import java.util.List;

public class ManagementTagService {
    private final MainRepository<Tag> tagRepository = new MainRepository<>(Tag.class, "tag");
    private final FirestoreRealtimeUtil realtimeUtil = new FirestoreRealtimeUtil();
    private ListenerRegistration tagListener;

    public void getAllTags(Observer<List<Tag>> observer) {
        Query query = FirebaseFirestore.getInstance().collection("tag");
        tagRepository.getAll(query).observeForever(observer);
    }

    public void addTag(Tag tag, CallBackUtil.OnInsertCallback callback) {
        tagRepository.add(tag, "tag", callback);
    }

    public void updateTag(Tag tag, CallBackUtil.OnUpdateCallback callback) {
        tagRepository.update(tag, tag.getId().getId(), callback);
    }

    public void deleteTag(Tag tag, CallBackUtil.OnDeleteCallBack callback) {
        tagRepository.delete(tag.getId().getId(), callback);
    }

    public void listenRealtime(FirestoreRealtimeUtil.RealtimeListener<Tag> listener) {
        tagListener = realtimeUtil.listenToTags(listener);
    }

    public void removeRealtimeListener() {
        if (tagListener != null) tagListener.remove();
        realtimeUtil.removeAllListeners();
    }
} 