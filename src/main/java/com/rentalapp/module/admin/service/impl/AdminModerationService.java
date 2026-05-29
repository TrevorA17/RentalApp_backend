package com.rentalapp.module.admin.service.impl;

import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.admin.dto.AdminListingResponse;
import com.rentalapp.module.admin.dto.AdminModerationActionResponse;
import com.rentalapp.module.admin.dto.AdminUserResponse;
import com.rentalapp.module.admin.dto.UpdateListingApprovalRequest;
import com.rentalapp.module.admin.dto.UpdateUserStatusRequest;
import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import com.rentalapp.module.admin.service.IAdminModerationService;
import com.rentalapp.module.admin.service.IModerationAuditService;
import com.rentalapp.module.admin.repository.IModerationActionRepository;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.IUserRepository;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.IListingRepository;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminModerationService implements IAdminModerationService {
    private final IListingRepository listingRepository;
    private final IUserRepository userRepository;
    private final IModerationActionRepository moderationActionRepository;
    private final IModerationAuditService moderationAuditService;

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

        ApprovalStatus previousApprovalStatus = listing.getApprovalStatus();
        listing.setApprovalStatus(request.getApprovalStatus());
        if (request.getApprovalStatus() == ApprovalStatus.APPROVED) {
            listing.setListingStatus(ListingStatus.PUBLISHED);
        } else if (request.getApprovalStatus() == ApprovalStatus.REJECTED && listing.getListingStatus() == ListingStatus.PUBLISHED) {
            listing.setListingStatus(ListingStatus.DRAFT);
        }

        Listing savedListing = listingRepository.save(listing);
        moderationAuditService.recordStatusChange(
                ModerationTargetType.LISTING,
                savedListing.getId(),
                toListingActionType(previousApprovalStatus, savedListing.getApprovalStatus()),
                previousApprovalStatus.name(),
                savedListing.getApprovalStatus().name(),
                null
        );

        return toAdminListing(savedListing);
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

    @Override
    @Transactional(readOnly = true)
    public List<AdminModerationActionResponse> getRecentModerationActions(int limit) {
        SecurityUtils.requireRole(Role.ADMIN);
        int normalizedLimit = Math.max(1, Math.min(limit, 50));
        return moderationActionRepository.findRecentWithActor()
                .stream()
                .limit(normalizedLimit)
                .map(action -> AdminModerationActionResponse.builder()
                        .id(action.getId())
                        .targetType(action.getTargetType())
                        .targetId(action.getTargetId())
                        .actionType(action.getActionType())
                        .previousStatus(action.getPreviousStatus())
                        .newStatus(action.getNewStatus())
                        .reasonOrNote(action.getReasonOrNote())
                        .createdAt(action.getCreatedAt())
                        .actor(AdminModerationActionResponse.ActorSummary.builder()
                                .userId(action.getActorUser().getId())
                                .fullName(action.getActorUser().getFullName())
                                .email(action.getActorUser().getEmail())
                                .build())
                        .build())
                .toList();
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

    private ModerationActionType toListingActionType(ApprovalStatus previousStatus, ApprovalStatus newStatus) {
        if (newStatus == ApprovalStatus.APPROVED) {
            return ModerationActionType.APPROVE;
        }

        if (newStatus == ApprovalStatus.REJECTED) {
            return ModerationActionType.REJECT;
        }

        return previousStatus == newStatus ? ModerationActionType.STATUS_CHANGE : ModerationActionType.FLAG;
    }
}
