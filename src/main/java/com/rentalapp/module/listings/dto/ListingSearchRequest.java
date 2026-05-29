package com.rentalapp.module.listings.dto;

import com.rentalapp.module.listings.entity.HouseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingSearchRequest {
    private String city;
    private String area;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer bedrooms;
    private Integer bathrooms;
    private HouseType houseType;
    private Boolean furnished;
    private List<String> amenities;
    private Integer page;
    private Integer perPage;
    private ListingSortOption sort = ListingSortOption.PUBLISHED_AT_DESC;
}
