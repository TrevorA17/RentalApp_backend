package com.rentalapp.module.recommendations.repository;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.recommendations.entity.AgentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRecommendationRepository extends JpaRepository<AgentRecommendation, String> {
    List<AgentRecommendation> findByAgentUser_IdAndApprovalStatusOrderByCreatedAtDesc(String agentUserId, ApprovalStatus approvalStatus);

    Optional<AgentRecommendation> findByAgentUser_IdAndAuthorUser_Id(String agentUserId, String authorUserId);

    List<AgentRecommendation> findAllByOrderByCreatedAtDesc();
}
