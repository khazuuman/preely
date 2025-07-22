package com.example.preely.viewmodel;

import androidx.lifecycle.Observer;
import com.example.preely.model.entities.Post;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.CallBackUtil;
import com.example.preely.util.FirestoreRealtimeUtil;
import com.example.preely.util.ImageUploadUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import android.content.Context;
import android.net.Uri;
import java.util.List;

public class ManagementPostService {
    private final MainRepository<Post> postRepository = new MainRepository<>(Post.class, "post");
    private final FirestoreRealtimeUtil realtimeUtil = new FirestoreRealtimeUtil();
    private ListenerRegistration postListener;

    public void getAllPosts(Observer<List<Post>> observer) {
        Query query = FirebaseFirestore.getInstance().collection("post");
        postRepository.getAll(query).observeForever(observer);
    }

    public void addPost(Post post, CallBackUtil.OnInsertCallback callback) {
        postRepository.add(post, "post", callback);
    }

    public void addPostAndUploadImages(Post post, List<Uri> imageUris, Context context, ImageUploadUtil.ImageUploadCallback imageCallback) {
        postRepository.add(post, "post", new CallBackUtil.OnInsertCallback() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if (imageUris != null && !imageUris.isEmpty()) {
                    ImageUploadUtil imageUploadUtil = new ImageUploadUtil(context);
                    String postId = documentReference.getId();
                    imageUploadUtil.uploadMultipleImagesForPost(imageUris.toArray(new Uri[0]), postId, imageCallback);
                }
            }
            @Override
            public void onFailure(Exception e) {
                if (imageCallback != null) imageCallback.onError(e.getMessage());
            }
        });
    }

    public void updatePost(Post post, CallBackUtil.OnUpdateCallback callback) {
        postRepository.update(post, post.getId().getId(), callback);
    }

    public void deletePost(Post post, CallBackUtil.OnDeleteCallBack callback) {
        postRepository.delete(post.getId().getId(), callback);
    }

    public void listenRealtime(FirestoreRealtimeUtil.RealtimeListener<Post> listener) {
        postListener = realtimeUtil.listenToPosts(listener);
    }

    public void removeRealtimeListener() {
        if (postListener != null) postListener.remove();
        realtimeUtil.removeAllListeners();
    }
} 