package com.example.preely.model.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedPostDTO {
    private String id;
    private String title;
    private String imageUrl;
    private double rating;
    private int ratingCount;
    private double price;
    private Date savedAt;
    private boolean isSaved;


} 