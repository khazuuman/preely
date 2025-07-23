package com.example.preely.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDto extends CommonDto {
    String title;
    String description;
    double price;
    String categoryName;
    String providerName;
    String status;
    float rating;
} 