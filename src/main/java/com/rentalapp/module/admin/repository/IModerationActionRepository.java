package com.rentalapp.module.admin.repository;

import com.rentalapp.module.admin.entity.ModerationAction;
import com.rentalapp.module.admin.entity.ModerationTargetType;

import java.util.List;

public interface IModerationActionRepository {
    ModerationAction save(ModerationAction action);
    List<ModerationAction> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(ModerationTargetType targetType, String targetId);
    List<ModerationAction> findRecentWithActor();
}
