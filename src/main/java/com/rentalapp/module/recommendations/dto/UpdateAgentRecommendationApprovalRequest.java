package com.rentalapp.module.recommendations.dto;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAgentRecommendationApprovalRequest {
    @NotNull(message = "Approval status is required.")
    private ApprovalStatus approvalStatus;
}
