package com.rentalapp.module.recommendations.dto;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AgentRecommendationResponse {
    private final String id;
    private final int rating;
    private final String comment;
    private final ApprovalStatus approvalStatus;
    private final Instant createdAt;
    private final AuthorSummary author;

    @Getter
    @Builder
    public static class AuthorSummary {
        private final String userId;
        private final String fullName;
        private final String role;
    }
}
