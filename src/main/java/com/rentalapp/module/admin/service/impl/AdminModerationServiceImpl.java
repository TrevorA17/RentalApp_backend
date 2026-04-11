package com.rentalapp.module.admin.service.impl;

import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.admin.dto.AdminListingResponse;
import com.rentalapp.module.admin.dto.AdminUserResponse;
import com.rentalapp.module.admin.dto.UpdateListingApprovalRequest;
import com.rentalapp.module.admin.dto.UpdateUserStatusRequest;
import com.rentalapp.module.admin.service.AdminModerationService;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminModerationServiceImpl implements AdminModerationService {
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminListingResponse> getListingsForModeration() {
        SecurityUtils.requireRole(Role.ADMIN);
        return listingRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Listing::getUpdatedAt).reversed())
                .map(this::toAdminListing)
                .toList();
    }

    @Override
    @Transactional
    public AdminListingResponse updateListingApproval(String listingId, UpdateListingApprovalRequest request) {
        SecurityUtils.requireRole(Role.ADMIN);
        Listing listing = listingRepository.findWithOwnerUserAndAmenitiesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found."));

        listing.setApprovalStatus(request.getApprovalStatus());
        if (request.getApprovalStatus() == ApprovalStatus.APPROVED) {
            listing.setListingStatus(ListingStatus.PUBLISHED);
        } else if (request.getApprovalStatus() == ApprovalStatus.REJECTED && listing.getListingStatus() == ListingStatus.PUBLISHED) {
            listing.setListingStatus(ListingStatus.DRAFT);
        }

        return toAdminListing(listingRepository.save(listing));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers() {
        SecurityUtils.requireRole(Role.ADMIN);
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(this::toAdminUser)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
        SecurityUtils.requireRole(Role.ADMIN);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setStatus(request.getStatus());
        return toAdminUser(userRepository.save(user));
    }

    private AdminListingResponse toAdminListing(Listing listing) {
        return AdminListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .city(listing.getCity())
                .area(listing.getArea())
                .listingStatus(listing.getListingStatus())
                .approvalStatus(listing.getApprovalStatus())
                .updatedAt(listing.getUpdatedAt())
                .owner(AdminListingResponse.OwnerSummary.builder()
                        .userId(listing.getOwnerUser().getId())
                        .fullName(listing.getOwnerUser().getFullName())
                        .email(listing.getOwnerUser().getEmail())
                        .build())
                .build();
    }

    private AdminUserResponse toAdminUser(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
