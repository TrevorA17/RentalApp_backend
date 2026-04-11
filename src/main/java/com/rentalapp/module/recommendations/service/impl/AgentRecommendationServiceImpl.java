package com.rentalapp.module.recommendations.service.impl;

import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.recommendations.dto.AgentRecommendationResponse;
import com.rentalapp.module.recommendations.dto.CreateAgentRecommendationRequest;
import com.rentalapp.module.recommendations.dto.UpdateAgentRecommendationApprovalRequest;
import com.rentalapp.module.recommendations.entity.AgentRecommendation;
import com.rentalapp.module.recommendations.repository.AgentRecommendationRepository;
import com.rentalapp.module.recommendations.service.AgentRecommendationService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentRecommendationServiceImpl implements AgentRecommendationService {
    private final AgentRecommendationRepository agentRecommendationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AgentRecommendationResponse createRecommendation(String agentUserId, CreateAgentRecommendationRequest request) {
        String authorUserId = SecurityUtils.requireCurrentUserId();
        User authorUser = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        User agentUser = userRepository.findById(agentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found."));

        if (agentUser.getRole() != Role.AGENT) {
            throw new ValidationException("Recommendations can only be submitted for agent profiles.");
        }

        if (authorUser.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Admin accounts cannot submit agent recommendations.");
        }

        if (authorUser.getId().equals(agentUserId)) {
            throw new ValidationException("You cannot submit a recommendation for your own profile.");
        }

        if (agentRecommendationRepository.findByAgentUser_IdAndAuthorUser_Id(agentUserId, authorUserId).isPresent()) {
            throw new ValidationException("You have already submitted a recommendation for this agent.");
        }

        AgentRecommendation recommendation = new AgentRecommendation();
        recommendation.setAgentUser(agentUser);
        recommendation.setAuthorUser(authorUser);
        recommendation.setRating(request.getRating());
        recommendation.setComment(normalizeComment(request.getComment()));
        recommendation.setApprovalStatus(ApprovalStatus.APPROVED);

        return toResponse(agentRecommendationRepository.save(recommendation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentRecommendationResponse> getPublicRecommendations(String agentUserId) {
        User agentUser = userRepository.findById(agentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found."));

        if (agentUser.getRole() != Role.AGENT) {
            throw new ValidationException("Recommendations can only be viewed for agent profiles.");
        }

        return agentRecommendationRepository.findByAgentUser_IdAndApprovalStatusOrderByCreatedAtDesc(agentUserId, ApprovalStatus.APPROVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentRecommendationResponse> getAdminRecommendations() {
        SecurityUtils.requireRole(Role.ADMIN);
        return agentRecommendationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AgentRecommendationResponse updateApprovalStatus(String recommendationId, UpdateAgentRecommendationApprovalRequest request) {
        SecurityUtils.requireRole(Role.ADMIN);
        AgentRecommendation recommendation = agentRecommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found."));
        recommendation.setApprovalStatus(request.getApprovalStatus());
        return toResponse(agentRecommendationRepository.save(recommendation));
    }

    private String normalizeComment(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("Recommendation comment is required.");
        }

        return trimmed;
    }

    private AgentRecommendationResponse toResponse(AgentRecommendation recommendation) {
        return AgentRecommendationResponse.builder()
                .id(recommendation.getId())
                .rating(recommendation.getRating())
                .comment(recommendation.getComment())
                .approvalStatus(recommendation.getApprovalStatus())
                .createdAt(recommendation.getCreatedAt())
                .author(AgentRecommendationResponse.AuthorSummary.builder()
                        .userId(recommendation.getAuthorUser().getId())
                        .fullName(recommendation.getAuthorUser().getFullName())
                        .role(recommendation.getAuthorUser().getRole().name())
                        .build())
                .build();
    }
}
