package com.rentalapp.module.saved.service.impl;

import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.IUserRepository;
import com.rentalapp.module.listings.dto.AmenityResponse;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.entity.Amenity;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.IListingRepository;
import com.rentalapp.module.saved.entity.SavedListing;
import com.rentalapp.module.saved.repository.ISavedListingRepository;
import com.rentalapp.module.saved.service.ISavedListingService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedListingService implements ISavedListingService {
    private final ISavedListingRepository savedListingRepository;
    private final IListingRepository listingRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public void saveListing(String listingId) {
        User user = requireUser();
        Listing listing = requirePublicListing(listingId);

        if (savedListingRepository.existsByUser_IdAndListing_Id(user.getId(), listing.getId())) {
            return;
        }

        SavedListing savedListing = new SavedListing();
        savedListing.setUser(user);
        savedListing.setListing(listing);
        savedListingRepository.save(savedListing);
    }

    @Override
    @Transactional
    public void removeSavedListing(String listingId) {
        String userId = SecurityUtils.requireCurrentUserId();
        savedListingRepository.findByUser_IdAndListing_Id(userId, listingId)
                .ifPresent(savedListingRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingSummaryResponse> getSavedListings() {
        String userId = SecurityUtils.requireCurrentUserId();
        return savedListingRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(SavedListing::getListing)
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSavedListingIds() {
        String userId = SecurityUtils.requireCurrentUserId();
        return savedListingRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(savedListing -> savedListing.getListing().getId())
                .distinct()
                .toList();
    }

    private User requireUser() {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Admins cannot save listings.");
        }

        return user;
    }

    private Listing requirePublicListing(String listingId) {
        Listing listing = listingRepository.findWithOwnerUserAndAmenitiesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found."));

        boolean isPublic = listing.getListingStatus() == ListingStatus.PUBLISHED
                && listing.getApprovalStatus() == ApprovalStatus.APPROVED;

        if (!isPublic) {
            throw new ForbiddenException("Only published listings can be saved.");
        }

        return listing;
    }

    private ListingSummaryResponse toSummary(Listing listing) {
        return ListingSummaryResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .rentAmount(listing.getRentAmount())
                .depositAmount(listing.getDepositAmount())
                .agentFeeAmount(listing.getAgentFeeAmount())
                .city(listing.getCity())
                .area(listing.getArea())
                .bedrooms(listing.getBedrooms())
                .bathrooms(listing.getBathrooms())
                .houseType(listing.getHouseType())
                .furnished(listing.isFurnished())
                .availabilityStatus(listing.getAvailabilityStatus())
                .listingStatus(listing.getListingStatus())
                .approvalStatus(listing.getApprovalStatus())
                .ownerType(listing.getOwnerType())
                .amenities(listing.getAmenities().stream().map(this::toAmenity).toList())
                .build();
    }

    private AmenityResponse toAmenity(Amenity amenity) {
        return AmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .slug(amenity.getSlug())
                .build();
    }
}
