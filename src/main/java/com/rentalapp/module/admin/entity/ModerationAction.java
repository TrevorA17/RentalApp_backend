package com.rentalapp.module.admin.entity;

import com.rentalapp.common.entity.BaseEntity;
import com.rentalapp.module.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "moderation_actions", indexes = {
        @Index(name = "idx_moderation_actions_target_created_at", columnList = "target_type, target_id, created_at"),
        @Index(name = "idx_moderation_actions_actor_user_id", columnList = "actor_user_id")
})
public class ModerationAction extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private ModerationTargetType targetType;

    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ModerationActionType actionType;

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "reason_or_note", columnDefinition = "TEXT")
    private String reasonOrNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actorUser;
}
