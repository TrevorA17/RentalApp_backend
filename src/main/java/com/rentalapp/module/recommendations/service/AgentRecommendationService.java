package com.rentalapp.module.recommendations.service;

import com.rentalapp.module.recommendations.dto.AgentRecommendationResponse;
import com.rentalapp.module.recommendations.dto.CreateAgentRecommendationRequest;
import com.rentalapp.module.recommendations.dto.UpdateAgentRecommendationApprovalRequest;

import java.util.List;

public interface AgentRecommendationService {
    AgentRecommendationResponse createRecommendation(String agentUserId, CreateAgentRecommendationRequest request);

    List<AgentRecommendationResponse> getPublicRecommendations(String agentUserId);

    List<AgentRecommendationResponse> getAdminRecommendations();

    AgentRecommendationResponse updateApprovalStatus(String recommendationId, UpdateAgentRecommendationApprovalRequest request);
}
