package com.rentalapp.module.listings.dto;

import com.rentalapp.module.listings.entity.HouseType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
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
    private Integer size;
    private ListingSortOption sort = ListingSortOption.PUBLISHED_AT_DESC;
}
