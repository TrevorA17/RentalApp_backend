package com.rentalapp.module.admin.repository;

import com.rentalapp.module.admin.entity.ModerationAction;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, String> {
    List<ModerationAction> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(ModerationTargetType targetType, String targetId);
}
