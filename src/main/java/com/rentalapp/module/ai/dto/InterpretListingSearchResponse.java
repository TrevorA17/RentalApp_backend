package com.rentalapp.module.ai.dto;

import com.rentalapp.module.listings.entity.HouseType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class InterpretListingSearchResponse {
    private final String normalizedQuery;
    private final boolean interpreted;
    private final String provider;
    private final List<String> matchedSignals;
    private final List<String> notes;
    private final Filters filters;

    @Getter
    @Builder
    public static class Filters {
        private final String city;
        private final String area;
        private final BigDecimal minPrice;
        private final BigDecimal maxPrice;
        private final Integer bedrooms;
        private final Integer bathrooms;
        private final HouseType houseType;
        private final Boolean furnished;
        private final List<AmenityMatch> amenities;
    }

    @Getter
    @Builder
    public static class AmenityMatch {
        private final String id;
        private final String name;
    }
}
