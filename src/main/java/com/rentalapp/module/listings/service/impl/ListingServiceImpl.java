package com.rentalapp.module.listings.service.impl;

import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.dto.AmenityResponse;
import com.rentalapp.module.listings.dto.ListingDetailResponse;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.dto.ListingUpsertRequest;
import com.rentalapp.module.listings.entity.Amenity;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.AmenityRepository;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.module.listings.service.ListingService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {
    private final ListingRepository listingRepository;
    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ListingSummaryResponse createListing(ListingUpsertRequest request) {
        User owner = requirePoster();
        Listing listing = new Listing();
        listing.setOwnerUser(owner);
        listing.setOwnerType(owner.getRole());
        applyRequest(listing, request);
        return toSummary(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public ListingSummaryResponse updateListing(String listingId, ListingUpsertRequest request) {
        Listing listing = requireOwnedListing(listingId);
        applyRequest(listing, request);
        listing.setListingStatus(listing.getListingStatus() == ListingStatus.ARCHIVED ? ListingStatus.ARCHIVED : ListingStatus.DRAFT);
        listing.setApprovalStatus(ApprovalStatus.PENDING);
        listing.setPublishedAt(null);
        return toSummary(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public ListingSummaryResponse publishListing(String listingId) {
        Listing listing = requireOwnedListing(listingId);
        validatePublishable(listing);
        listing.setListingStatus(ListingStatus.PUBLISHED);
        listing.setApprovalStatus(ApprovalStatus.APPROVED);
        listing.setPublishedAt(Instant.now());
        return toSummary(listingRepository.save(listing));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingSummaryResponse> getMyListings() {
        String userId = SecurityUtils.requireCurrentUserId();
        requirePoster();
        return listingRepository.findByOwnerUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ListingDetailResponse getListingById(String listingId) {
        Listing listing = listingRepository.findWithOwnerUserAndAmenitiesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found."));

        String currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        boolean isOwner = currentUserId != null && listing.getOwnerUser().getId().equals(currentUserId);
        boolean isPublic = listing.getListingStatus() == ListingStatus.PUBLISHED && listing.getApprovalStatus() == ApprovalStatus.APPROVED;

        if (!isPublic && !isOwner) {
            throw new ForbiddenException("You do not have access to this listing.");
        }

        return toDetail(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> getAmenities() {
        return amenityRepository.findAll()
                .stream()
                .map(this::toAmenity)
                .toList();
    }

    private User requirePoster() {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.getRole() != Role.AGENT && user.getRole() != Role.LANDLORD) {
            throw new ForbiddenException("Only agents and landlords can manage listings.");
        }

        return user;
    }

    private Listing requireOwnedListing(String listingId) {
        User owner = requirePoster();
        Listing listing = listingRepository.findWithOwnerUserAndAmenitiesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found."));

        if (!listing.getOwnerUser().getId().equals(owner.getId())) {
            throw new ForbiddenException("Only the listing owner can modify this listing.");
        }

        return listing;
    }

    private void applyRequest(Listing listing, ListingUpsertRequest request) {
        List<Amenity> amenities = amenityRepository.findByIdIn(request.getAmenityIds());
        if (amenities.size() != request.getAmenityIds().size()) {
            throw new ValidationException("One or more selected amenities are invalid.");
        }

        listing.setTitle(request.getTitle().trim());
        listing.setDescription(request.getDescription().trim());
        listing.setRentAmount(request.getRentAmount());
        listing.setDepositAmount(request.getDepositAmount());
        listing.setAgentFeeAmount(request.getAgentFeeAmount());
        listing.setCity(request.getCity().trim());
        listing.setArea(request.getArea().trim());
        listing.setBedrooms(request.getBedrooms());
        listing.setBathrooms(request.getBathrooms());
        listing.setHouseType(request.getHouseType());
        listing.setFurnished(request.getFurnished());
        listing.setAvailabilityStatus(request.getAvailabilityStatus());
        listing.setAmenities(new LinkedHashSet<>(amenities));
    }

    private void validatePublishable(Listing listing) {
        if (listing.getTitle() == null || listing.getDescription() == null || listing.getAmenities().isEmpty()) {
            throw new ValidationException("Listing does not meet the minimum completeness threshold.");
        }
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

    private ListingDetailResponse toDetail(Listing listing) {
        return ListingDetailResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
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
                .poster(ListingDetailResponse.PosterSummary.builder()
                        .userId(listing.getOwnerUser().getId())
                        .fullName(listing.getOwnerUser().getFullName())
                        .email(listing.getOwnerUser().getEmail())
                        .role(listing.getOwnerUser().getRole())
                        .build())
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
