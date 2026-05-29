package com.rentalapp.module.reports.service.impl;

import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import com.rentalapp.module.admin.service.IModerationAuditService;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.module.reports.dto.CreateReportRequest;
import com.rentalapp.module.reports.dto.ReportResponse;
import com.rentalapp.module.reports.dto.UpdateReportStatusRequest;
import com.rentalapp.module.reports.entity.Report;
import com.rentalapp.module.reports.repository.ReportRepository;
import com.rentalapp.module.reports.service.IReportService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final IModerationAuditService moderationAuditService;

    @Override
    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        String reporterUserId = SecurityUtils.requireCurrentUserId();
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if ((request.getListingId() == null || request.getListingId().isBlank())
                && (request.getReportedUserId() == null || request.getReportedUserId().isBlank())) {
            throw new ValidationException("A report must target a listing or a user.");
        }

        Report report = new Report();
        report.setReporterUser(reporter);
        report.setReason(request.getReason().trim());
        report.setDetails(normalize(request.getDetails()));

        if (request.getListingId() != null && !request.getListingId().isBlank()) {
            Listing listing = listingRepository.findWithOwnerUserAndAmenitiesById(request.getListingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Listing not found."));
            report.setListing(listing);
            if (report.getReportedUser() == null) {
                report.setReportedUser(listing.getOwnerUser());
            }
        }

        if (request.getReportedUserId() != null && !request.getReportedUserId().isBlank()) {
            User reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reported user not found."));
            report.setReportedUser(reportedUser);
        }

        return toResponse(reportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getAdminReports() {
        SecurityUtils.requireRole(Role.ADMIN);
        return reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReportResponse updateReportStatus(String reportId, UpdateReportStatusRequest request) {
        SecurityUtils.requireRole(Role.ADMIN);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found."));
        String previousStatus = report.getStatus().name();
        report.setStatus(request.getStatus());
        Report savedReport = reportRepository.save(report);
        moderationAuditService.recordStatusChange(
                ModerationTargetType.REPORT,
                savedReport.getId(),
                toReportActionType(savedReport.getStatus()),
                previousStatus,
                savedReport.getStatus().name(),
                null
        );
        return toResponse(savedReport);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ReportResponse toResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .reporter(toUserSummary(report.getReporterUser()))
                .reportedUser(report.getReportedUser() == null ? null : toUserSummary(report.getReportedUser()))
                .listing(report.getListing() == null ? null : ReportResponse.ListingSummary.builder()
                        .listingId(report.getListing().getId())
                        .title(report.getListing().getTitle())
                        .city(report.getListing().getCity())
                        .area(report.getListing().getArea())
                        .ownerUserId(report.getListing().getOwnerUser().getId())
                        .ownerFullName(report.getListing().getOwnerUser().getFullName())
                        .build())
                .build();
    }

    private ReportResponse.UserSummary toUserSummary(User user) {
        return ReportResponse.UserSummary.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private ModerationActionType toReportActionType(com.rentalapp.module.reports.entity.ReportStatus newStatus) {
        return switch (newStatus) {
            case RESOLVED -> ModerationActionType.RESOLVE;
            case DISMISSED -> ModerationActionType.REJECT;
            case OPEN -> ModerationActionType.FLAG;
        };
    }
}
