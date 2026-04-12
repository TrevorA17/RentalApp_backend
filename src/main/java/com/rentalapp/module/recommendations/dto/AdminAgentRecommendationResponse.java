package com.rentalapp.module.recommendations.dto;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AdminAgentRecommendationResponse {
    String id;
    int rating;
    String comment;
    ApprovalStatus approvalStatus;
    Instant createdAt;
    AgentSummary agent;
    AuthorSummary author;

    @Value
    @Builder
    public static class AgentSummary {
        String userId;
        String fullName;
        String email;
    }

    @Value
    @Builder
    public static class AuthorSummary {
        String userId;
        String fullName;
        String email;
        String role;
    }
}
