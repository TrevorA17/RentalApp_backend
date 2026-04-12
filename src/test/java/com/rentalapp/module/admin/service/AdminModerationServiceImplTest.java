package com.rentalapp.module.admin.service;

import com.rentalapp.module.admin.dto.UpdateListingApprovalRequest;
import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import com.rentalapp.module.admin.service.impl.AdminModerationServiceImpl;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceImplTest {
    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModerationAuditService moderationAuditService;

    @InjectMocks
    private AdminModerationServiceImpl adminModerationService;

    @Test
    void updateListingApprovalWritesModerationAuditRecord() {
        User owner = new User();
        owner.setId("owner-1");
        owner.setFullName("Owner One");
        owner.setEmail("owner@example.com");

        Listing listing = new Listing();
        listing.setId("listing-1");
        listing.setApprovalStatus(ApprovalStatus.PENDING);
        listing.setListingStatus(ListingStatus.DRAFT);
        listing.setOwnerUser(owner);
        listing.setTitle("Title");
        listing.setCity("Nairobi");
        listing.setArea("Kilimani");

        UpdateListingApprovalRequest request = new UpdateListingApprovalRequest();
        request.setApprovalStatus(ApprovalStatus.APPROVED);

        when(listingRepository.findWithOwnerUserAndAmenitiesById("listing-1")).thenReturn(Optional.of(listing));
        when(listingRepository.save(listing)).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(() -> SecurityUtils.requireRole(Role.ADMIN)).thenAnswer(invocation -> null);

            var response = adminModerationService.updateListingApproval("listing-1", request);

            assertEquals(ApprovalStatus.APPROVED, response.getApprovalStatus());
            verify(moderationAuditService).recordStatusChange(
                    eq(ModerationTargetType.LISTING),
                    eq("listing-1"),
                    eq(ModerationActionType.APPROVE),
                    eq("PENDING"),
                    eq("APPROVED"),
                    eq(null)
            );
        }
    }
}
