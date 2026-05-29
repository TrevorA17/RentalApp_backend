package com.rentalapp.module.recommendations.repository;

import com.rentalapp.module.recommendations.entity.AgentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRecommendationRepository extends JpaRepository<AgentRecommendation, String>, IAgentRecommendationRepository {
}
