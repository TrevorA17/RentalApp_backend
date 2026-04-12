package com.rentalapp.module.admin.dto;

import com.rentalapp.module.listings.entity.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateListingApprovalRequest {
    @NotNull(message = "Approval status is required.")
    private ApprovalStatus approvalStatus;
}
