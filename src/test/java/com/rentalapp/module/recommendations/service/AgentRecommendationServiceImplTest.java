package com.rentalapp.module.recommendations.service;

import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import com.rentalapp.module.admin.service.ModerationAuditService;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.recommendations.dto.UpdateAgentRecommendationApprovalRequest;
import com.rentalapp.module.recommendations.entity.AgentRecommendation;
import com.rentalapp.module.recommendations.repository.AgentRecommendationRepository;
import com.rentalapp.module.recommendations.service.impl.AgentRecommendationServiceImpl;
import com.rentalapp.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRecommendationServiceImplTest {
    @Mock
    private AgentRecommendationRepository agentRecommendationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModerationAuditService moderationAuditService;

    @InjectMocks
    private AgentRecommendationServiceImpl agentRecommendationService;

    @Test
    void updateApprovalStatusWritesModerationAuditRecord() {
        User author = new User();
        author.setId("author-1");
        author.setFullName("Author One");
        author.setEmail("author@example.com");
        author.setRole(Role.RENTER);

        User agent = new User();
        agent.setId("agent-1");
        agent.setFullName("Agent One");
        agent.setEmail("agent@example.com");
        agent.setRole(Role.AGENT);

        AgentRecommendation recommendation = new AgentRecommendation();
        recommendation.setId("recommendation-1");
        recommendation.setApprovalStatus(ApprovalStatus.PENDING);
        recommendation.setAgentUser(agent);
        recommendation.setAuthorUser(author);
        recommendation.setComment("Very reliable");
        recommendation.setRating((short) 5);

        UpdateAgentRecommendationApprovalRequest request = new UpdateAgentRecommendationApprovalRequest();
        request.setApprovalStatus(ApprovalStatus.APPROVED);

        when(agentRecommendationRepository.findById("recommendation-1")).thenReturn(Optional.of(recommendation));
        when(agentRecommendationRepository.save(recommendation)).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(() -> SecurityUtils.requireRole(Role.ADMIN)).thenAnswer(invocation -> null);

            var response = agentRecommendationService.updateApprovalStatus("recommendation-1", request);

            assertEquals(ApprovalStatus.APPROVED, response.getApprovalStatus());
            verify(moderationAuditService).recordStatusChange(
                    eq(ModerationTargetType.AGENT_RECOMMENDATION),
                    eq("recommendation-1"),
                    eq(ModerationActionType.APPROVE),
                    eq("PENDING"),
                    eq("APPROVED"),
                    eq(null)
            );
        }
    }
}
