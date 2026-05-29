package com.rentalapp.module.admin.dto;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateListingApprovalRequest {
    @NotNull(message = "Approval status is required.")
    private ApprovalStatus approvalStatus;
}
