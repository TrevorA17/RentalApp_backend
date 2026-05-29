package com.rentalapp.module.admin.service;

import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;

public interface IModerationAuditService {
    void recordStatusChange(
            ModerationTargetType targetType,
            String targetId,
            ModerationActionType actionType,
            String previousStatus,
            String newStatus,
            String reasonOrNote
    );
}
