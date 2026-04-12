package com.rentalapp.module.reports.service;

import com.rentalapp.module.reports.dto.CreateReportRequest;
import com.rentalapp.module.reports.dto.ReportResponse;
import com.rentalapp.module.reports.dto.UpdateReportStatusRequest;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(CreateReportRequest request);
    List<ReportResponse> getAdminReports();
    ReportResponse updateReportStatus(String reportId, UpdateReportStatusRequest request);
}
