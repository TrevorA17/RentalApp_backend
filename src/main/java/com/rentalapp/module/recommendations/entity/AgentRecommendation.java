package com.rentalapp.module.recommendations.entity;

import com.rentalapp.common.entity.BaseEntity;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.listings.entity.ApprovalStatus;
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
@Table(name = "agent_recommendations", indexes = {
        @Index(name = "idx_agent_recommendations_agent_status", columnList = "agent_user_id, approvalStatus, createdAt"),
        @Index(name = "idx_agent_recommendations_author_user_id", columnList = "author_user_id")
})
public class AgentRecommendation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_user_id", nullable = false)
    private User agentUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User authorUser;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private short rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;
}
