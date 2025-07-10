package com.example.preely.repository;

import com.example.preely.util.Constraints;

public class CategoryRepository extends MainRepository{

    public CategoryRepository() {
        this.setTableName(Constraints.CollectionName.CATEGORIES);
    }

}
