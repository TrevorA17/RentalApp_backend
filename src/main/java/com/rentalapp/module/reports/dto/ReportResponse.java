package com.rentalapp.module.reports.dto;

import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.reports.entity.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ReportResponse {
    private final String id;
    private final String reason;
    private final String details;
    private final ReportStatus status;
    private final Instant createdAt;
    private final UserSummary reporter;
    private final UserSummary reportedUser;
    private final ListingSummary listing;

    @Getter
    @Builder
    public static class UserSummary {
        private final String userId;
        private final String fullName;
        private final String email;
        private final Role role;
    }

    @Getter
    @Builder
    public static class ListingSummary {
        private final String listingId;
        private final String title;
        private final String city;
        private final String area;
        private final String ownerUserId;
        private final String ownerFullName;
    }
}
