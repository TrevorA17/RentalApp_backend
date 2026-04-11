package com.rentalapp.module.reports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReportRequest {
    private String listingId;
    private String reportedUserId;

    @NotBlank(message = "Reason is required.")
    @Size(max = 120, message = "Reason must not exceed 120 characters.")
    private String reason;

    @Size(max = 2000, message = "Details must not exceed 2000 characters.")
    private String details;
}
