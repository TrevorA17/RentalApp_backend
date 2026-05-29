package com.rentalapp.module.inquiries.service;

import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.IUserRepository;
import com.rentalapp.module.inquiries.dto.CreateInquiryRequest;
import com.rentalapp.module.inquiries.dto.UpdateInquiryStatusRequest;
import com.rentalapp.module.inquiries.entity.Inquiry;
import com.rentalapp.module.inquiries.entity.InquiryStatus;
import com.rentalapp.module.inquiries.repository.IInquiryRepository;
import com.rentalapp.module.inquiries.service.impl.InquiryService;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.IListingRepository;
import com.rentalapp.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {
    @Mock
    private IInquiryRepository inquiryRepository;

    @Mock
    private IListingRepository listingRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    void createInquiryRejectsOwnListing() {
        User owner = buildUser("owner-1", Role.AGENT);
        Listing listing = buildListing(owner, ListingStatus.PUBLISHED, ApprovalStatus.APPROVED);
        CreateInquiryRequest request = new CreateInquiryRequest();
        request.setMessage("Interested");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("owner-1");
            when(userRepository.findById("owner-1")).thenReturn(Optional.of(owner));
            when(listingRepository.findWithOwnerUserAndAmenitiesById("listing-1")).thenReturn(Optional.of(listing));

            assertThrows(ForbiddenException.class, () -> inquiryService.createInquiry("listing-1", request));
        }
    }

    @Test
    void createInquiryRejectsUnpublishedListing() {
        User sender = buildUser("sender-1", Role.RENTER);
        User owner = buildUser("owner-1", Role.LANDLORD);
        Listing listing = buildListing(owner, ListingStatus.DRAFT, ApprovalStatus.PENDING);
        CreateInquiryRequest request = new CreateInquiryRequest();
        request.setMessage("Interested");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("sender-1");
            when(userRepository.findById("sender-1")).thenReturn(Optional.of(sender));
            when(listingRepository.findWithOwnerUserAndAmenitiesById("listing-1")).thenReturn(Optional.of(listing));

            assertThrows(ForbiddenException.class, () -> inquiryService.createInquiry("listing-1", request));
        }
    }

    @Test
    void updateInquiryStatusRejectsNonRecipient() {
        User sender = buildUser("sender-1", Role.RENTER);
        User recipient = buildUser("recipient-1", Role.AGENT);
        Listing listing = buildListing(recipient, ListingStatus.PUBLISHED, ApprovalStatus.APPROVED);
        Inquiry inquiry = new Inquiry();
        inquiry.setId("inq-1");
        inquiry.setListing(listing);
        inquiry.setSenderUser(sender);
        inquiry.setRecipientUser(recipient);
        inquiry.setStatus(InquiryStatus.NEW);
        inquiry.setMessage("Interested");

        UpdateInquiryStatusRequest request = new UpdateInquiryStatusRequest();
        request.setStatus(InquiryStatus.CONTACTED);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("someone-else");
            when(inquiryRepository.findById("inq-1")).thenReturn(Optional.of(inquiry));

            assertThrows(ForbiddenException.class, () -> inquiryService.updateInquiryStatus("inq-1", request));
        }
    }

    @Test
    void createInquiryPersistsTrimmedMessage() {
        User sender = buildUser("sender-1", Role.RENTER);
        User owner = buildUser("owner-1", Role.LANDLORD);
        Listing listing = buildListing(owner, ListingStatus.PUBLISHED, ApprovalStatus.APPROVED);
        CreateInquiryRequest request = new CreateInquiryRequest();
        request.setMessage("  Interested in viewing this place.  ");

        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("sender-1");
            when(userRepository.findById("sender-1")).thenReturn(Optional.of(sender));
            when(listingRepository.findWithOwnerUserAndAmenitiesById("listing-1")).thenReturn(Optional.of(listing));

            var response = inquiryService.createInquiry("listing-1", request);

            assertEquals("Interested in viewing this place.", response.getMessage());
            assertEquals("listing-1", response.getListingId());
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

    private Listing buildListing(User owner, ListingStatus listingStatus, ApprovalStatus approvalStatus) {
        Listing listing = new Listing();
        listing.setId("listing-1");
        listing.setTitle("Listing title");
        listing.setRentAmount(new BigDecimal("35000"));
        listing.setOwnerUser(owner);
        listing.setListingStatus(listingStatus);
        listing.setApprovalStatus(approvalStatus);
        return listing;
    }
}
