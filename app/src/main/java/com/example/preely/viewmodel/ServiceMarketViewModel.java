package com.example.preely.viewmodel;

import androidx.lifecycle.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.User;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.response.ServiceMarketResponse;
import com.example.preely.util.DataUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.preely.util.Constraints.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceMarketViewModel extends ViewModel {
    //    private static final MainRepository<SavedPost> savedPostRepository = new MainRepository<>(SavedPost.class, CollectionName.SAVED_POST);
    private final MutableLiveData<List<ServiceMarketResponse>> serviceMarketResponseListResult = new MutableLiveData<>();
    //    private final MutableLiveData<PostResponse> postResponseResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLastPageResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> postExisted = new MutableLiveData<>();
    private DocumentSnapshot lastVisible = null;
    private static final int PAGE_SIZE = 6;

    public LiveData<Boolean> getIsLastPageResult() {
        return isLastPageResult;
    }

    public LiveData<Boolean> getPostExisted() {
        return postExisted;
    }

    public LiveData<List<ServiceMarketResponse>> getServiceListResult() {
        return serviceMarketResponseListResult;
    }

    public void resetPostListResult() {
        serviceMarketResponseListResult.postValue(new ArrayList<>());
    }

//    public LiveData<PostResponse> getPostResult() {
//        return postResponseResult;
//    }

    private Query buildPostQuery(ServiceFilterRequest request) {
        Query query = FirebaseFirestore.getInstance().collection(CollectionName.SERVICE);
        if (request.getSortType() != null) {
            switch (request.getSortType()) {
                case SortType.MOST_REVIEW:
                    query = query.orderBy("total_reviews", Query.Direction.DESCENDING);
                    break;
                case SortType.DATE_ASC:
                    query = query.orderBy("create_at", Query.Direction.ASCENDING);
                    break;
                case SortType.DATE_DESC:
                    query = query.orderBy("create_at", Query.Direction.DESCENDING);
                    break;
                case SortType.PRICE_ASC:
                    query = query.orderBy("price", Query.Direction.ASCENDING);
                    break;
                case SortType.PRICE_DESC:
                    query = query.orderBy("price", Query.Direction.DESCENDING);
                    break;
            }
        } else {
            query = query.orderBy("create_at", Query.Direction.DESCENDING);
        }
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("title", request.getTitle())
                    .whereLessThan("title", request.getTitle() + '\uf8ff');
        }
        if (request.getCategory_ids() != null && !request.getCategory_ids().isEmpty()) {
            query = query.whereIn("category_id", request.getCategory_ids());
        }
        if (request.getRating() != null) {
            query = query.whereGreaterThanOrEqualTo("rating", request.getRating());
        }
        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            query = query.whereArrayContainsAny("availability", request.getAvailability());
        }
        query = query.limit(ServiceMarketViewModel.PAGE_SIZE);
        return query;
    }

    private Task<ServiceMarketResponse> buildPostResponseTask(Service service) {
        ServiceMarketResponse serviceMarketResponse;
        try {
            serviceMarketResponse = DataUtil.mapObj(service, ServiceMarketResponse.class);
            if (service.getImage_urls() != null && !service.getImage_urls().isEmpty()) {
                serviceMarketResponse.setImage(service.getImage_urls().get(0));
            }
            Log.i("SERVICE", serviceMarketResponse.toString());
        } catch (Exception e) {
            return Tasks.forException(e);
        }
        List<Task<?>> subTasks = new ArrayList<>();
        // category name
        if (service.getCategory_id() != null) {
            Task<DocumentSnapshot> categoryTask = service.getCategory_id().get();
            subTasks.add(categoryTask);
            categoryTask.addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Category category = doc.toObject(Category.class);
                    assert category != null;
                    Log.i("CATEGORY", category.toString());
                    serviceMarketResponse.setCategoryName(category.getName());
                }
            });
        }
        //  provider
        if (service.getProvider_id() != null) {
            Task<DocumentSnapshot> providerTask = service.getProvider_id().get();
            subTasks.add(providerTask);
            providerTask.addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    User provider = snapshot.toObject(User.class);
                    assert provider != null;
                    Log.i("USER", provider.toString());
                    serviceMarketResponse.setProviderName(provider.getFull_name());
                }
            });
        }

        return Tasks.whenAll(subTasks)
                .continueWith(task -> serviceMarketResponse);
    }

    public void getServiceList(ServiceFilterRequest request) {

        Query query = buildPostQuery(request);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnSuccessListener(querySnapshot -> {
            Log.i("QUERY SNAPSHOT", querySnapshot.toString());
            if (!querySnapshot.isEmpty()) {
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                lastVisible = documents.get(documents.size() - 1);
                Log.i("LAST VISIBLE", lastVisible.toString());

                List<Task<ServiceMarketResponse>> servicesTask = new ArrayList<>();
                for (DocumentSnapshot serviceDoc : documents) {
                    Service service = serviceDoc.toObject(Service.class);
                    assert service != null;
                    Log.i("POST", service.toString());
                    Task<ServiceMarketResponse> combinedTask = buildPostResponseTask(service);
                    servicesTask.add(combinedTask);
                }

                Tasks.whenAllSuccess(servicesTask).addOnSuccessListener(results -> {
                    List<ServiceMarketResponse> finalList = new ArrayList<>();
                    for (Object obj : results) {
                        finalList.add((ServiceMarketResponse) obj);
                    }
                    Log.i("FINAL LIST", finalList.toString());
                    if (finalList.size() < PAGE_SIZE) {
                        Log.i("IS LAST PAGE", "last page true");
                        isLastPageResult.setValue(true);
                    } else {
                        Log.i("IS LAST PAGE", "last page false");
                        isLastPageResult.setValue(false);
                    }
                    serviceMarketResponseListResult.setValue(finalList);
                });
            } else {
                Log.i("IS LAST PAGE", "last page true");
                isLastPageResult.setValue(true);
            }
        });
    }

    public void resetPagination() {
        lastVisible = null;
        isLastPageResult.setValue(false);
    }

    // saved post
//    public void insertSavedPost(SavedPostRequest request) throws IllegalAccessException, InstantiationException {
//        Query query = FirebaseFirestore.getInstance()
//                .collection(CollectionName.SAVED_POST)
//                .whereEqualTo("user_id", request.getUser_id())
//                .whereEqualTo("post_id", request.getPost_id())
//                .limit(1);
//        savedPostRepository.getOne(query).observeForever(result -> {
//            if (result == null) {
//                try {
//                    insertSavedPostDetail(request);
//                } catch (IllegalAccessException | InstantiationException e) {
//                    throw new RuntimeException(e);
//                }
//                postExisted.setValue(false);
//                Log.i("GET SAVED POST", "Saved post insert successfully");
//            } else {
//                postExisted.setValue(true);
//                Log.i("GET SAVED POST", "Saved post already exists");
//            }
//        });
//    }

    private final MutableLiveData<Map<String, Boolean>> savedPostsStatus = new MutableLiveData<>(new HashMap<>());

//    public LiveData<Map<String, Boolean>> getSavedPostsStatus() {
//        return savedPostsStatus;
//    }
//
//    public void checkSavedPost(SavedPostRequest request) {
//        Query query = FirebaseFirestore.getInstance()
//                .collection(CollectionName.SAVED_POST)
//                .whereEqualTo("user_id", request.getUser_id())
//                .whereEqualTo("post_id", request.getPost_id())
//                .limit(1);
//        savedPostRepository.getOne(query).observeForever(result -> {
//            Map<String, Boolean> currentMap = savedPostsStatus.getValue();
//            if (currentMap == null) currentMap = new HashMap<>();
//
//            String key = request.getUser_id() + "_" + request.getPost_id();
//            currentMap.put(key, result != null);
//
//            savedPostsStatus.postValue(currentMap);
//        });
//    }


//    public void insertSavedPostDetail(SavedPostRequest request) throws IllegalAccessException, InstantiationException {
//        SavedPost savedPost = DataUtil.mapObj(request, SavedPost.class);
//        savedPost.setSave_date(Timestamp.now());
//        Map<String, Object> map = excludeBaseTimestamps(savedPost);
//        savedPostRepository.getDb().collection(CollectionName.SAVED_POST).add(map)
//                .addOnSuccessListener(documentReference -> {
//                    Log.i("INSERT SAVED POST", "Saved post insert successfully");
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("INSERT SAVED POST", "Failed to insert saved post");
//                });
//    }

//    public Map<String, Object> excludeBaseTimestamps(SavedPost savedPost) {
//        Map<String, Object> map = new HashMap<>();
//
//        map.put("post_id", savedPost.getPost_id());
//        map.put("save_date", savedPost.getSave_date());
//        map.put("user_id", savedPost.getUser_id());
//
//        return map;
//    }
//
//    public void getPost(String postRef) {
//        DocumentReference postRefDoc = FirebaseFirestore.getInstance()
//                .collection(CollectionName.POSTS)
//                .document(postRef);
//        AtomicReference<PostResponse> postResponse = new AtomicReference<>(new PostResponse());
//        postRefDoc.get().addOnSuccessListener(documentSnapshot -> {
//            if (documentSnapshot.exists()) {
//                Post post = documentSnapshot.toObject(Post.class);
//                try {
//                    assert post != null;
//                    postResponse.set(DataUtil.mapObj(post, PostResponse.class));
//                    if (post.getCategory_id() != null) {
//                        Task<DocumentSnapshot> categoryTask = post.getCategory_id().get();
//                        categoryTask.addOnSuccessListener(doc -> {
//                            if (doc.exists()) {
//                                Category category = doc.toObject(Category.class);
//                                try {
//                                    assert category != null;
//                                    postResponse.get().setCategoryResponse(DataUtil.mapObj(category, CategoryResponse.class));
//                                } catch (IllegalAccessException | InstantiationException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }
//                        });
//                    }
//                } catch (IllegalAccessException | InstantiationException e) {
//                    throw new RuntimeException(e);
//                }
//                if (post.getTag_ids() != null && !post.getTag_ids().isEmpty()) {
//                    Task<QuerySnapshot> tagsTask = FirebaseFirestore.getInstance()
//                            .collection(CollectionName.TAGS)
//                            .whereIn(FieldPath.documentId(), post.getTag_ids())
//                            .get();
//                    tagsTask.addOnSuccessListener(snapshot -> {
//                        List<TagResponse> tagResponses = new ArrayList<>();
//                        for (DocumentSnapshot tagDoc : snapshot.getDocuments()) {
//                            try {
//                                Tag tag = tagDoc.toObject(Tag.class);
//                                assert tag != null;
//                                tagResponses.add(DataUtil.mapObj(tag, TagResponse.class));
//                            } catch (IllegalAccessException | InstantiationException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                        postResponse.get().setTagResponses(tagResponses);
//                    });
//                }
//                postResponseResult.setValue(postResponse.get());
//            } else {
//                postResponseResult.setValue(null);
//            }
//        });
//    }

}
