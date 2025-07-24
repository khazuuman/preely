package com.example.preely.model.request;

import com.google.firebase.firestore.DocumentReference;
import java.util.List;

public class ServiceFilterRequest {
    private String query;
    private List<String> categoryIds;
    private Integer sortType;

    public ServiceFilterRequest() {}
    public ServiceFilterRequest(String query, List<String> categoryIds, Integer sortType) {
        this.query = query;
        this.categoryIds = categoryIds;
        this.sortType = sortType;
    }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }
    public Integer getSortType() { return sortType; }
    public void setSortType(Integer sortType) { this.sortType = sortType; }
} 