package com.example.preely.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.preely.model.Category;
import com.example.preely.repository.CategoryRepository;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository = new CategoryRepository();

    //test get all categories
    public void getAllCategories() {
        categoryRepository.getAll(Category.class, data -> {
            for (Category c : data) {
                Log.i("Category", c.toString());
            }
        });
    }

}
