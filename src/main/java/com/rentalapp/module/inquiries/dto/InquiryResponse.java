package com.rentalapp.module.inquiries.dto;

import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.inquiries.entity.InquiryStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class InquiryResponse {
    private final String id;
    private final String listingId;
    private final String listingTitle;
    private final BigDecimal listingRentAmount;
    private final String message;
    private final InquiryStatus status;
    private final Instant createdAt;
    private final UserSummary sender;
    private final UserSummary recipient;

    @Getter
    @Builder
    public static class UserSummary {
        private final String userId;
        private final String fullName;
        private final String email;
        private final Role role;
    }
}
