package com.rentalapp.module.reports.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.reports.dto.CreateReportRequest;
import com.rentalapp.module.reports.dto.ReportResponse;
import com.rentalapp.module.reports.dto.UpdateReportStatusRequest;
import com.rentalapp.module.reports.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/api/v1/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(@Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Report submitted successfully.", reportService.createReport(request)));
    }

    @GetMapping("/api/v1/admin/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getAdminReports() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAdminReports()));
    }

    @PatchMapping("/api/v1/admin/reports/{reportId}/status")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReportStatus(
            @PathVariable String reportId,
            @Valid @RequestBody UpdateReportStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Report status updated successfully.", reportService.updateReportStatus(reportId, request)));
    }
}
