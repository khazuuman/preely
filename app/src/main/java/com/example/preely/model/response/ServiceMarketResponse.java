package com.example.preely.model.response;

import com.example.preely.util.Constraints;

import java.util.List;

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
public class ServiceMarketResponse extends CommonResponse {
    String title;
    String providerName;
    String categoryName;
    String status;
    Float average_rating;
    String image;
    Double price;
    Constraints.PriceUnitType price_unit;
    List<SkillResponse> skills;
    Constraints.Availability availability;

} 