package com.example.preely.viewmodel;

import androidx.lifecycle.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.SavedService;
import com.example.preely.model.entities.Service;
import com.example.preely.model.entities.User;
import com.example.preely.model.request.SavedServiceRequest;
import com.example.preely.model.request.ServiceFilterRequest;
import com.example.preely.model.response.ServiceMarketDetailResponse;
import com.example.preely.model.response.ServiceMarketResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.DataUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.preely.util.Constraints.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceMarketViewModel extends ViewModel {
    private static final MainRepository<SavedService> savedPostRepository = new MainRepository<>(SavedService.class, CollectionName.SAVED_SERVICE);
    private final MutableLiveData<List<ServiceMarketResponse>> serviceMarketResponseListResult = new MutableLiveData<>();
    private final MutableLiveData<ServiceMarketDetailResponse> detailResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLastPageResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSavedServiceExisted = new MutableLiveData<>();
    private DocumentSnapshot lastVisible = null;
    private static final int PAGE_SIZE = 6;

    public LiveData<Boolean> getIsLastPageResult() {
        return isLastPageResult;
    }

    public LiveData<Boolean> getIsSavedServiceExisted() {
        return isSavedServiceExisted;
    }

    public LiveData<List<ServiceMarketResponse>> getServiceListResult() {
        return serviceMarketResponseListResult;
    }

    public void resetPostListResult() {
        serviceMarketResponseListResult.postValue(new ArrayList<>());
    }

    public LiveData<ServiceMarketDetailResponse> getDetailResponse() {
        return detailResponse;
    }

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
            query = query.whereGreaterThanOrEqualTo("average_rating", request.getRating());
        }
        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            Log.i("AVAILABILITY", request.getAvailability().toString());
            query = query.whereIn("availability", request.getAvailability());
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
                serviceMarketResponseListResult.setValue(null);
            }
        });
    }

    public void resetPagination() {
        lastVisible = null;
        isLastPageResult.setValue(false);
    }

    // saved post
    public void checkSavedPost(SavedServiceRequest request) throws IllegalAccessException, InstantiationException {
        Query query = FirebaseFirestore.getInstance()
                .collection(CollectionName.SAVED_SERVICE)
                .whereEqualTo("user_id", request.getUser_id())
                .whereEqualTo("service_id", request.getService_id())
                .limit(1);
        savedPostRepository.getOne(query).observeForever(result -> {
            if (result == null) {
                try {
                    insertSavedService(request);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
                isSavedServiceExisted.setValue(false);
                Log.i("GET SAVED SERVICE", "Saved service insert successfully");
            } else {
                isSavedServiceExisted.setValue(true);
                Log.i("GET SAVED SERVICE", "Saved service already exists");
            }
        });
    }

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


    public void insertSavedService(SavedServiceRequest request) throws IllegalAccessException, InstantiationException {
        Map<String, Object> map = excludeBaseTimestamps(request);
        savedPostRepository.getDb().collection(CollectionName.SAVED_SERVICE).add(map)
                .addOnSuccessListener(documentReference -> {
                    Log.i("INSERT SAVED SERVICE", "Saved service insert successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("INSERT SAVED SERVICE", "Failed to insert saved service");
                });
    }

    public Map<String, Object> excludeBaseTimestamps(SavedServiceRequest request) {
        Map<String, Object> map = new HashMap<>();

        map.put("service_id", request.getService_id());
        map.put("user_id", request.getUser_id());

        return map;
    }

    public void getServiceDetail(String serviceId) {
        DocumentReference serviceRefDoc = FirebaseFirestore.getInstance()
                .collection(CollectionName.SERVICE)
                .document(serviceId);

        Log.i("SERVICE REF", serviceId);

        serviceRefDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Service service = documentSnapshot.toObject(Service.class);
                try {
                    assert service != null;
                    ServiceMarketDetailResponse response = DataUtil.mapObj(service, ServiceMarketDetailResponse.class);

                    // Map location từ Service entity
                    response.setLocation(service.getLocation());
                    Log.d("ServiceMarketViewModel", "Service location: " + service.getLocation());

                    List<Task<?>> tasks = new ArrayList<>();

                    // Map category name
                    if (service.getCategory_id() != null) {
                        Task<DocumentSnapshot> categoryTask = service.getCategory_id().get();
                        tasks.add(categoryTask);
                        categoryTask.addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                Category category = doc.toObject(Category.class);
                                if (category != null) {
                                    response.setCategoryName(category.getName());
                                }
                            }
                        });
                    }

                    // Map provider name
                    if (service.getProvider_id() != null) {
                        Task<DocumentSnapshot> providerTask = service.getProvider_id().get();
                        tasks.add(providerTask);
                        providerTask.addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                User provider = snapshot.toObject(User.class);
                                if (provider != null) {
                                    response.setProviderName(
                                            provider.getFull_name() == null ? provider.getUsername() : provider.getFull_name()
                                    );
                                }
                            }
                        });
                    }

                    // Set response sau khi hoàn thành tất cả tasks
                    if (tasks.isEmpty()) {
                        detailResponse.setValue(response);
                    } else {
                        Tasks.whenAllComplete(tasks).addOnSuccessListener(taskList -> {
                            detailResponse.setValue(response);
                            Log.d("ServiceMarketViewModel", "Service detail loaded with location");
                        });
                    }

                } catch (IllegalAccessException | InstantiationException e) {
                    Log.e("ServiceMarketViewModel", "Error mapping service detail: " + e.getMessage());
                    detailResponse.setValue(null);
                }
            } else {
                Log.w("ServiceMarketViewModel", "Service not found: " + serviceId);
                detailResponse.setValue(null);
            }
        }).addOnFailureListener(e -> {
            Log.e("ServiceMarketViewModel", "Error loading service detail: " + e.getMessage());
            detailResponse.setValue(null);
        });
    }
}
