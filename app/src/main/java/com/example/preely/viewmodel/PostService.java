package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Post;
import com.example.preely.model.entities.SavedPost;
import com.example.preely.model.entities.Tag;
import com.example.preely.model.request.PostFilterRequest;
import com.example.preely.model.request.SavedPostRequest;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.model.response.PostResponse;
import com.example.preely.model.response.TagResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.DataUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.preely.util.Constraints.*;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostService extends ViewModel {
    private static final MainRepository<SavedPost> savedPostRepository = new MainRepository<>(SavedPost.class, CollectionName.SAVED_POST);
    private final MutableLiveData<List<PostResponse>> postResponseListResult = new MutableLiveData<>();
    private final MutableLiveData<String> insertSavedPostResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLastPageResult = new MutableLiveData<>();
    private DocumentSnapshot lastVisible = null;
    private static final int PAGE_SIZE = 6;
    public LiveData<Boolean> getIsLastPageResult() {
        return isLastPageResult;
    }
    public LiveData<List<PostResponse>> getPostListResult() {
        return postResponseListResult;
    }

    public LiveData<String> getInsertSavedPostResult() {
        return insertSavedPostResult;
    }

    private Query buildPostQuery(PostFilterRequest request) {
        Query query = FirebaseFirestore.getInstance().collection(CollectionName.POSTS);
        if (request.getCategory_id() != null && !request.getCategory_id().isEmpty()) {
            query = query.whereIn("category_id", request.getCategory_id());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            query = query.whereIn("status", request.getStatus());
        }
        if (request.getWard() != null) {
            query = query.whereEqualTo("ward", request.getWard());
        }
        if (request.getProvince() != null) {
            query = query.whereEqualTo("province", request.getProvince());
        }
        if (request.getTag_id() != null && !request.getTag_id().isEmpty()) {
            query = query.whereArrayContainsAny("tag_ids", request.getTag_id());
        }
        query = query.orderBy("create_at", Query.Direction.DESCENDING);
        query = query.limit(PostService.PAGE_SIZE);
        return query;
    }

    private Task<PostResponse> buildPostResponseTask(Post post) {
        PostResponse postResponse;
        try {
            postResponse = DataUtil.mapObj(post, PostResponse.class);
        } catch (Exception e) {
            return Tasks.forException(e);
        }
        List<Task<?>> subTasks = new ArrayList<>();
        // category
        if (post.getCategory_id() != null) {
            Task<DocumentSnapshot> categoryTask = post.getCategory_id().get();
            subTasks.add(categoryTask);
            categoryTask.addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Category category = doc.toObject(Category.class);
                    try {
                        assert category != null;
                        postResponse.setCategoryResponse(DataUtil.mapObj(category, CategoryResponse.class));
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        // tags
        if (post.getTag_ids() != null && !post.getTag_ids().isEmpty()) {
            Task<QuerySnapshot> tagsTask = FirebaseFirestore.getInstance()
                    .collection(CollectionName.TAGS)
                    .whereIn(FieldPath.documentId(), post.getTag_ids())
                    .get();
            subTasks.add(tagsTask);
            tagsTask.addOnSuccessListener(snapshot -> {
                List<TagResponse> tagResponses = new ArrayList<>();
                for (DocumentSnapshot tagDoc : snapshot.getDocuments()) {
                    try {
                        Tag tag = tagDoc.toObject(Tag.class);
                        assert tag != null;
                        tagResponses.add(DataUtil.mapObj(tag, TagResponse.class));
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
                postResponse.setTagResponses(tagResponses);
            });
        }
        return Tasks.whenAll(subTasks)
                .continueWith(task -> postResponse);
    }

//    public void getPostList(PostFilterRequest request) {
//        Query query = buildPostQuery(request, PAGE_SIZE);
//
//        query.get().addOnSuccessListener(querySnapshot -> {
//            List<Task<PostResponse>> postTasks = new ArrayList<>();
//
//            for (DocumentSnapshot postDoc : querySnapshot.getDocuments()) {
//                Post post = postDoc.toObject(Post.class);
//                Task<PostResponse> combinedTask = buildPostResponseTask(post);
//                postTasks.add(combinedTask);
//            }
//            Tasks.whenAllSuccess(postTasks).addOnSuccessListener(results -> {
//                List<PostResponse> finalList = new ArrayList<>();
//                for (Object obj : results) {
//                    finalList.add((PostResponse) obj);
//                }
//                postResponseListResult.setValue(finalList);
//            });
//        });
//    }

    public void getPostList(PostFilterRequest request) {
        Query query = buildPostQuery(request);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                lastVisible = documents.get(documents.size() - 1);

                List<Task<PostResponse>> postTasks = new ArrayList<>();
                for (DocumentSnapshot postDoc : documents) {
                    Post post = postDoc.toObject(Post.class);
                    Task<PostResponse> combinedTask = buildPostResponseTask(post);
                    postTasks.add(combinedTask);
                }

                Tasks.whenAllSuccess(postTasks).addOnSuccessListener(results -> {
                    List<PostResponse> finalList = new ArrayList<>();
                    for (Object obj : results) {
                        finalList.add((PostResponse) obj);
                    }

                    if (finalList.size() < PAGE_SIZE) {
                        isLastPageResult.setValue(true);
                    }
                    postResponseListResult.setValue(finalList);
                });
            } else {
                isLastPageResult.setValue(true);
            }
        });
    }


    // saved post
    public void insertSavedPost(SavedPostRequest request) throws IllegalAccessException, InstantiationException {
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.SAVED_POST)
                .whereEqualTo("user_id", request.getUser_id())
                .whereEqualTo("post_id", request.getPost_id())
                .limit(1);
        savedPostRepository.getOne(query).observeForever(result -> {
            if (result == null) {
                try {
                    insertSavedPostDetail(request);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
                insertSavedPostResult.setValue("Saved post insert successfully");
                Log.i("GET SAVED POST", "Saved post insert successfully");
            } else {
                insertSavedPostResult.setValue("Saved post already exists");
                Log.i("GET SAVED POST", "Saved post already exists");
            }
        });
    }

    public void insertSavedPostDetail(SavedPostRequest request) throws IllegalAccessException, InstantiationException {
        SavedPost savedPost = DataUtil.mapObj(request, SavedPost.class);
        savedPost.setSave_date(Timestamp.now());
        Map<String, Object> map = excludeBaseTimestamps(savedPost);
        savedPostRepository.getDb().collection(CollectionName.SAVED_POST).add(map)
                .addOnSuccessListener(documentReference -> {
                    Log.i("INSERT SAVED POST", "Saved post insert successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("INSERT SAVED POST", "Failed to insert saved post");
                });
    }

    public Map<String, Object> excludeBaseTimestamps(SavedPost savedPost) {
        Map<String, Object> map = new HashMap<>();

        map.put("post_id", savedPost.getPost_id());
        map.put("save_date", savedPost.getSave_date());
        map.put("user_id", savedPost.getUser_id());

        return map;
    }

}
