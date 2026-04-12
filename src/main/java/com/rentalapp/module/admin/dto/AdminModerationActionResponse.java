package com.rentalapp.module.admin.dto;

import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AdminModerationActionResponse {
    String id;
    ModerationTargetType targetType;
    String targetId;
    ModerationActionType actionType;
    String previousStatus;
    String newStatus;
    String reasonOrNote;
    Instant createdAt;
    ActorSummary actor;

    @Value
    @Builder
    public static class ActorSummary {
        String userId;
        String fullName;
        String email;
    }
}
