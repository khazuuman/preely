package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.preely.model.entities.Category;
import com.example.preely.model.entities.User;
import com.example.preely.model.response.CategoryResponse;
import com.example.preely.repository.MainRepository;
import com.example.preely.util.Constraints;
import com.example.preely.util.DataUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CategoryService extends ViewModel {

    public final MainRepository<Category> userRepository = new MainRepository<>(Category.class, Constraints.CollectionName.CATEGORIES);

    private final MutableLiveData<List<CategoryResponse>> cateListResult = new MutableLiveData<>();

    public LiveData<List<CategoryResponse>> getCateListResult() {
        return cateListResult;
    }

    public void getCateList() {
        Query query = FirebaseFirestore.getInstance()
                .collection(Constraints.CollectionName.CATEGORIES);
        userRepository.getAll(query).observeForever(categories -> {
            if (categories != null) {
                List<CategoryResponse> categoryResponses = new ArrayList<>();
                for (Category category : categories) {
                    try {
                        CategoryResponse categoryResponse = DataUtil.mapObj(category, CategoryResponse.class);
                        categoryResponses.add(categoryResponse);
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (categoryResponses.size() > 7) {
                    categoryResponses.add(7, new CategoryResponse("Khác...", ""));
                }
                cateListResult.setValue(categoryResponses);
                Log.i("CATE COUNT", String.valueOf(categoryResponses.size()));
            } else {
                cateListResult.setValue(null);
            }
        });
    }

}
