package com.rentalapp.module.recommendations.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.recommendations.dto.AgentRecommendationResponse;
import com.rentalapp.module.recommendations.dto.CreateAgentRecommendationRequest;
import com.rentalapp.module.recommendations.dto.UpdateAgentRecommendationApprovalRequest;
import com.rentalapp.module.recommendations.service.AgentRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AgentRecommendationController {
    private final AgentRecommendationService agentRecommendationService;

    @PostMapping("/api/v1/agents/{agentUserId}/recommendations")
    public ResponseEntity<ApiResponse<AgentRecommendationResponse>> createRecommendation(
            @PathVariable String agentUserId,
            @Valid @RequestBody CreateAgentRecommendationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Recommendation submitted successfully.",
                agentRecommendationService.createRecommendation(agentUserId, request)
        ));
    }

    @GetMapping("/api/v1/agents/{agentUserId}/recommendations")
    public ResponseEntity<ApiResponse<List<AgentRecommendationResponse>>> getPublicRecommendations(@PathVariable String agentUserId) {
        return ResponseEntity.ok(ApiResponse.ok(agentRecommendationService.getPublicRecommendations(agentUserId)));
    }

    @GetMapping("/api/v1/admin/recommendations")
    public ResponseEntity<ApiResponse<List<AgentRecommendationResponse>>> getAdminRecommendations() {
        return ResponseEntity.ok(ApiResponse.ok(agentRecommendationService.getAdminRecommendations()));
    }

    @PatchMapping("/api/v1/admin/recommendations/{recommendationId}/approval")
    public ResponseEntity<ApiResponse<AgentRecommendationResponse>> updateApprovalStatus(
            @PathVariable String recommendationId,
            @Valid @RequestBody UpdateAgentRecommendationApprovalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Recommendation moderation updated.",
                agentRecommendationService.updateApprovalStatus(recommendationId, request)
        ));
    }
}
