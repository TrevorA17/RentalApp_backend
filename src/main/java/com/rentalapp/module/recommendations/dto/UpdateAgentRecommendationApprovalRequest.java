package com.rentalapp.module.recommendations.dto;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentRecommendationApprovalRequest {
    @NotNull(message = "Approval status is required.")
    private ApprovalStatus approvalStatus;
}
