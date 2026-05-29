package com.rentalapp.module.recommendations.service;

import com.rentalapp.module.recommendations.dto.AgentRecommendationResponse;
import com.rentalapp.module.recommendations.dto.AdminAgentRecommendationResponse;
import com.rentalapp.module.recommendations.dto.CreateAgentRecommendationRequest;
import com.rentalapp.module.recommendations.dto.UpdateAgentRecommendationApprovalRequest;

import java.util.List;

public interface IAgentRecommendationService {
    AgentRecommendationResponse createRecommendation(String agentUserId, CreateAgentRecommendationRequest request);

    List<AgentRecommendationResponse> getPublicRecommendations(String agentUserId);

    List<AdminAgentRecommendationResponse> getAdminRecommendations();

    AdminAgentRecommendationResponse updateApprovalStatus(String recommendationId, UpdateAgentRecommendationApprovalRequest request);
}
