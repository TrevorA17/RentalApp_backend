package com.rentalapp.module.reports.service;

import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import com.rentalapp.module.admin.service.ModerationAuditService;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.module.reports.dto.CreateReportRequest;
import com.rentalapp.module.reports.dto.UpdateReportStatusRequest;
import com.rentalapp.module.reports.entity.Report;
import com.rentalapp.module.reports.repository.ReportRepository;
import com.rentalapp.module.reports.service.impl.ReportServiceImpl;
import com.rentalapp.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {
    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ModerationAuditService moderationAuditService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void createReportRequiresTarget() {
        User reporter = buildUser("reporter-1", Role.RENTER);
        CreateReportRequest request = new CreateReportRequest();
        request.setReason("Suspicious listing");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("reporter-1");
            when(userRepository.findById("reporter-1")).thenReturn(Optional.of(reporter));

            assertThrows(ValidationException.class, () -> reportService.createReport(request));
        }
    }

    @Test
    void createReportUsesListingOwnerWhenReportedUserNotProvided() {
        User reporter = buildUser("reporter-1", Role.RENTER);
        User owner = buildUser("owner-1", Role.AGENT);
        Listing listing = new Listing();
        listing.setId("listing-1");
        listing.setTitle("Listing title");
        listing.setCity("Nairobi");
        listing.setArea("Kilimani");
        listing.setOwnerUser(owner);

        CreateReportRequest request = new CreateReportRequest();
        request.setListingId("listing-1");
        request.setReason("Suspicious listing");
        request.setDetails("  Needs review  ");

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("reporter-1");
            when(userRepository.findById("reporter-1")).thenReturn(Optional.of(reporter));
            when(listingRepository.findWithOwnerUserAndAmenitiesById("listing-1")).thenReturn(Optional.of(listing));

            var response = reportService.createReport(request);

            assertEquals("Suspicious listing", response.getReason());
            assertEquals("owner-1", response.getReportedUser().getUserId());
            assertEquals("Needs review", response.getDetails());
        }
    }

    @Test
    void updateReportStatusWritesModerationAuditRecord() {
        User reporter = buildUser("reporter-1", Role.RENTER);
        Report report = new Report();
        report.setId("report-1");
        report.setStatus(com.rentalapp.module.reports.entity.ReportStatus.OPEN);
        report.setReporterUser(reporter);
        report.setReason("Suspicious listing");

        UpdateReportStatusRequest request = new UpdateReportStatusRequest();
        request.setStatus(com.rentalapp.module.reports.entity.ReportStatus.RESOLVED);

        when(reportRepository.findById("report-1")).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(() -> SecurityUtils.requireRole(Role.ADMIN)).thenAnswer(invocation -> null);

            var response = reportService.updateReportStatus("report-1", request);

            assertEquals(com.rentalapp.module.reports.entity.ReportStatus.RESOLVED, response.getStatus());
            verify(moderationAuditService).recordStatusChange(
                    eq(ModerationTargetType.REPORT),
                    eq("report-1"),
                    eq(ModerationActionType.RESOLVE),
                    eq("OPEN"),
                    eq("RESOLVED"),
                    eq(null)
            );
        }
    }

    private User buildUser(String id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail(id + "@example.com");
        user.setFullName(id);
        return user;
    }
}
