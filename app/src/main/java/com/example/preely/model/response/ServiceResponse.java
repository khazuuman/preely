package com.example.preely.model.response;

import com.example.preely.model.dto.CommonDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponse extends CommonResponse {
    String title;
    String description;
    double price;
    String categoryName;
    String providerName;
    String status;
    float rating;
} 