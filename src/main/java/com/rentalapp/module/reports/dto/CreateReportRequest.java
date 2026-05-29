package com.rentalapp.module.reports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    private String listingId;
    private String reportedUserId;

    @NotBlank(message = "Reason is required.")
    @Size(max = 120, message = "Reason must not exceed 120 characters.")
    private String reason;

    @Size(max = 2000, message = "Details must not exceed 2000 characters.")
    private String details;
}
