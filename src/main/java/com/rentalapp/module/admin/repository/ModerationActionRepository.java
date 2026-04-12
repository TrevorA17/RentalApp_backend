package com.rentalapp.module.admin.repository;

import com.rentalapp.module.admin.entity.ModerationAction;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, String> {
    List<ModerationAction> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(ModerationTargetType targetType, String targetId);

    @Query("select m from ModerationAction m join fetch m.actorUser order by m.createdAt desc")
    List<ModerationAction> findRecentWithActor();
}
