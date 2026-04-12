package com.rentalapp.module.admin.dto;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.ListingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AdminListingResponse {
    private final String id;
    private final String title;
    private final String city;
    private final String area;
    private final ListingStatus listingStatus;
    private final ApprovalStatus approvalStatus;
    private final Instant updatedAt;
    private final OwnerSummary owner;

    @Getter
    @Builder
    public static class OwnerSummary {
        private final String userId;
        private final String fullName;
        private final String email;
    }
}
