package com.rentalapp.module.listings.dto;

import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.AvailabilityStatus;
import com.rentalapp.module.listings.entity.HouseType;
import com.rentalapp.module.listings.entity.ListingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ListingDetailResponse {
    private final String id;
    private final String title;
    private final String description;
    private final BigDecimal rentAmount;
    private final BigDecimal depositAmount;
    private final BigDecimal agentFeeAmount;
    private final String city;
    private final String area;
    private final Integer bedrooms;
    private final Integer bathrooms;
    private final HouseType houseType;
    private final boolean furnished;
    private final AvailabilityStatus availabilityStatus;
    private final ListingStatus listingStatus;
    private final ApprovalStatus approvalStatus;
    private final Role ownerType;
    private final List<AmenityResponse> amenities;
    private final PosterSummary poster;

    @Getter
    @Builder
    public static class PosterSummary {
        private final String userId;
        private final String fullName;
        private final String email;
        private final Role role;
    }
}
