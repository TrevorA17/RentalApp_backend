package com.rentalapp.module.inquiries.service.impl;

import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.inquiries.dto.CreateInquiryRequest;
import com.rentalapp.module.inquiries.dto.InquiryResponse;
import com.rentalapp.module.inquiries.dto.UpdateInquiryStatusRequest;
import com.rentalapp.module.inquiries.entity.Inquiry;
import com.rentalapp.module.inquiries.entity.InquiryStatus;
import com.rentalapp.module.inquiries.repository.InquiryRepository;
import com.rentalapp.module.inquiries.service.InquiryService;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {
    private final InquiryRepository inquiryRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public InquiryResponse createInquiry(String listingId, CreateInquiryRequest request) {
        User sender = requireSender();
        Listing listing = listingRepository.findWithOwnerUserAndAmenitiesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found."));

        boolean isPublic = listing.getListingStatus() == ListingStatus.PUBLISHED
                && listing.getApprovalStatus() == ApprovalStatus.APPROVED;
        if (!isPublic) {
            throw new ForbiddenException("You can only inquire about published listings.");
        }

        if (listing.getOwnerUser().getId().equals(sender.getId())) {
            throw new ForbiddenException("You cannot send an inquiry to your own listing.");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setListing(listing);
        inquiry.setSenderUser(sender);
        inquiry.setRecipientUser(listing.getOwnerUser());
        inquiry.setMessage(request.getMessage().trim());
        inquiry.setStatus(InquiryStatus.NEW);

        return toResponse(inquiryRepository.save(inquiry));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InquiryResponse> getSentInquiries() {
        String userId = SecurityUtils.requireCurrentUserId();
        return inquiryRepository.findBySenderUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InquiryResponse> getReceivedInquiries() {
        String userId = SecurityUtils.requireCurrentUserId();
        return inquiryRepository.findByRecipientUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InquiryResponse updateInquiryStatus(String inquiryId, UpdateInquiryStatusRequest request) {
        String userId = SecurityUtils.requireCurrentUserId();
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found."));

        if (!inquiry.getRecipientUser().getId().equals(userId)) {
            throw new ForbiddenException("Only the recipient can update inquiry status.");
        }

        inquiry.setStatus(request.getStatus());
        return toResponse(inquiryRepository.save(inquiry));
    }

    private User requireSender() {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Admins cannot send listing inquiries.");
        }

        return user;
    }

    private InquiryResponse toResponse(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .listingId(inquiry.getListing().getId())
                .listingTitle(inquiry.getListing().getTitle())
                .listingRentAmount(inquiry.getListing().getRentAmount())
                .message(inquiry.getMessage())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .sender(toUserSummary(inquiry.getSenderUser()))
                .recipient(toUserSummary(inquiry.getRecipientUser()))
                .build();
    }

    private InquiryResponse.UserSummary toUserSummary(User user) {
        return InquiryResponse.UserSummary.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
