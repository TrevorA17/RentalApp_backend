package com.rentalapp.module.reports.dto;

import com.rentalapp.module.reports.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportStatusRequest {
    @NotNull(message = "Status is required.")
    private ReportStatus status;
}
