package com.rentalapp.module.reports.dto;

import com.rentalapp.module.reports.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReportStatusRequest {
    @NotNull(message = "Status is required.")
    private ReportStatus status;
}
