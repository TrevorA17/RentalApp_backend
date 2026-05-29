package com.rentalapp.module.listings.service.impl;

import com.rentalapp.common.api.PaginatedResponse;
import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.IUserRepository;
import com.rentalapp.module.listings.dto.AmenityResponse;
import com.rentalapp.module.listings.dto.ListingDetailResponse;
import com.rentalapp.module.listings.dto.ListingSearchRequest;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.dto.ListingSortOption;
import com.rentalapp.module.listings.dto.ListingUpsertRequest;
import com.rentalapp.module.listings.entity.Amenity;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.media.dto.ListingMediaRequest;
import com.rentalapp.module.media.dto.ListingMediaResponse;
import com.rentalapp.module.media.entity.ListingMedia;
import com.rentalapp.module.media.repository.IListingMediaRepository;
import com.rentalapp.module.listings.repository.IAmenityRepository;
import com.rentalapp.module.listings.repository.IListingRepository;
import com.rentalapp.module.listings.service.IListingService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ListingService implements IListingService {
    private final IListingRepository listingRepository;
    private final IAmenityRepository amenityRepository;
    private final IListingMediaRepository listingMediaRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public ListingSummaryResponse createListing(ListingUpsertRequest request) {
        User owner = requirePoster();
        Listing listing = new Listing();
        listing.setOwnerUser(owner);
        listing.setOwnerType(owner.getRole());
        applyRequest(listing, request);
        Listing savedListing = listingRepository.save(listing);
        return toSummary(savedListing, savedListing.getMedia());
    }

    @Override
    @Transactional
    public ListingSummaryResponse updateListing(String listingId, ListingUpsertRequest request) {
        Listing listing = requireOwnedListing(listingId);
        applyRequest(listing, request);
        listing.setListingStatus(listing.getListingStatus() == ListingStatus.ARCHIVED ? ListingStatus.ARCHIVED : ListingStatus.DRAFT);
        listing.setApprovalStatus(ApprovalStatus.PENDING);
        listing.setPublishedAt(null);
        Listing savedListing = listingRepository.save(listing);
        return toSummary(savedListing, savedListing.getMedia());
    }

    @Override
    @Transactional
    public ListingSummaryResponse publishListing(String listingId) {
        Listing listing = requireOwnedListing(listingId);
        validatePublishable(listing);
        listing.setListingStatus(ListingStatus.PUBLISHED);
        listing.setApprovalStatus(ApprovalStatus.APPROVED);
        listing.setPublishedAt(Instant.now());
        Listing savedListing = listingRepository.save(listing);
        return toSummary(savedListing, savedListing.getMedia());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingSummaryResponse> getMyListings() {
        String userId = SecurityUtils.requireCurrentUserId();
        requirePoster();
        List<Listing> listings = listingRepository.findDistinctByOwnerUserIdOrderByUpdatedAtDesc(userId);
        Map<String, List<ListingMedia>> mediaByListingId = getMediaByListingId(listings);
        return listings
                .stream()
                .map(listing -> toSummary(listing, mediaByListingId.getOrDefault(listing.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ListingSummaryResponse> searchPublicListings(ListingSearchRequest request) {
        Specification<Listing> spec = Specification
                .where(hasListingStatus(ListingStatus.PUBLISHED))
                .and(hasApprovalStatus(ApprovalStatus.APPROVED))
                .and(hasCity(request.getCity()))
                .and(hasArea(request.getArea()))
                .and(hasMinPrice(request.getMinPrice()))
                .and(hasMaxPrice(request.getMaxPrice()))
                .and(hasBedrooms(request.getBedrooms()))
                .and(hasBathrooms(request.getBathrooms()))
                .and(hasHouseType(request.getHouseType()))
                .and(hasFurnished(request.getFurnished()))
                .and(hasAmenities(request.getAmenities()));

        Pageable pageable = buildSearchPageable(request);
        Page<Listing> listingsPage = listingRepository.findAll(spec, pageable);
        List<Listing> listings = listingsPage.getContent();
        Map<String, List<ListingMedia>> mediaByListingId = getMediaByListingId(listings);
        List<ListingSummaryResponse> items = listings
                .stream()
                .map(listing -> toSummary(listing, mediaByListingId.getOrDefault(listing.getId(), List.of())))
                .toList();

        return PaginatedResponse.<ListingSummaryResponse>builder()
                .items(items)
                .page(listingsPage.getNumber())
                .size(listingsPage.getSize())
                .totalElements(listingsPage.getTotalElements())
                .totalPages(listingsPage.getTotalPages())
                .hasNext(listingsPage.hasNext())
                .hasPrevious(listingsPage.hasPrevious())
                .sort((request.getSort() == null ? ListingSortOption.PUBLISHED_AT_DESC : request.getSort()).name())
                .build();
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

        List<ListingMedia> media = listingMediaRepository.findByListing_IdOrderByDisplayOrderAsc(listingId);
        return toDetail(listing, media);
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
        listing.getMedia().clear();
        List<ListingMediaRequest> mediaRequests = request.getMedia() == null ? List.of() : request.getMedia();
        for (int index = 0; index < mediaRequests.size(); index++) {
            ListingMediaRequest mediaRequest = mediaRequests.get(index);
            validateMediaRequest(mediaRequest);
            ListingMedia media = new ListingMedia();
            media.setListing(listing);
            media.setMediaType(mediaRequest.getMediaType());
            media.setMediaUrl(mediaRequest.getMediaUrl().trim());
            media.setCaption(mediaRequest.getCaption() == null || mediaRequest.getCaption().isBlank()
                    ? null
                    : mediaRequest.getCaption().trim());
            media.setDisplayOrder(index);
            listing.getMedia().add(media);
        }
    }

    private void validateMediaRequest(ListingMediaRequest mediaRequest) {
        if (mediaRequest.getMediaType() == null) {
            throw new ValidationException("Media type is required.");
        }

        String mediaUrl = mediaRequest.getMediaUrl() == null ? "" : mediaRequest.getMediaUrl().trim();
        if (mediaUrl.isBlank()) {
            throw new ValidationException("Media URL is required.");
        }

        try {
            URI uri = new URI(mediaUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ValidationException("Media URL must use http or https.");
            }
        } catch (URISyntaxException exception) {
            throw new ValidationException("Media URL is invalid.");
        }
    }

    private void validatePublishable(Listing listing) {
        if (listing.getTitle() == null || listing.getDescription() == null || listing.getAmenities().isEmpty()) {
            throw new ValidationException("Listing does not meet the minimum completeness threshold.");
        }
    }

    private Specification<Listing> hasListingStatus(ListingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("listingStatus"), status);
    }

    private Specification<Listing> hasApprovalStatus(ApprovalStatus status) {
        return (root, query, cb) -> cb.equal(root.get("approvalStatus"), status);
    }

    private Specification<Listing> hasCity(String city) {
        return (root, query, cb) ->
                city == null || city.isBlank() ? cb.conjunction() : cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase());
    }

    private Specification<Listing> hasArea(String area) {
        return (root, query, cb) ->
                area == null || area.isBlank() ? cb.conjunction() : cb.equal(cb.lower(root.get("area")), area.trim().toLowerCase());
    }

    private Specification<Listing> hasMinPrice(java.math.BigDecimal minPrice) {
        return (root, query, cb) ->
                minPrice == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("rentAmount"), minPrice);
    }

    private Specification<Listing> hasMaxPrice(java.math.BigDecimal maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("rentAmount"), maxPrice);
    }

    private Specification<Listing> hasBedrooms(Integer bedrooms) {
        return (root, query, cb) ->
                bedrooms == null ? cb.conjunction() : cb.equal(root.get("bedrooms"), bedrooms);
    }

    private Specification<Listing> hasBathrooms(Integer bathrooms) {
        return (root, query, cb) ->
                bathrooms == null ? cb.conjunction() : cb.equal(root.get("bathrooms"), bathrooms);
    }

    private Specification<Listing> hasHouseType(com.rentalapp.module.listings.entity.HouseType houseType) {
        return (root, query, cb) ->
                houseType == null ? cb.conjunction() : cb.equal(root.get("houseType"), houseType);
    }

    private Specification<Listing> hasFurnished(Boolean furnished) {
        return (root, query, cb) ->
                furnished == null ? cb.conjunction() : cb.equal(root.get("furnished"), furnished);
    }

    private Specification<Listing> hasAmenities(List<String> amenityIds) {
        return (root, query, cb) -> {
            if (amenityIds == null || amenityIds.isEmpty()) {
                return cb.conjunction();
            }

            query.distinct(true);
            return root.join("amenities").get("id").in(amenityIds);
        };
    }

    private Pageable buildSearchPageable(ListingSearchRequest request) {
        int requestedPage = request.getPage() == null ? 0 : request.getPage();
        int requestedSize = request.getSize() == null ? 12 : request.getSize();
        int normalizedPage = Math.max(0, requestedPage);
        int normalizedSize = Math.min(Math.max(1, requestedSize), 24);
        ListingSortOption sort = request.getSort() == null ? ListingSortOption.PUBLISHED_AT_DESC : request.getSort();
        return PageRequest.of(normalizedPage, normalizedSize, sort.toSort());
    }

    private ListingSummaryResponse toSummary(Listing listing, List<ListingMedia> media) {
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
                .thumbnailUrl(resolveThumbnailUrl(media))
                .media(toMediaResponses(media))
                .build();
    }

    private ListingDetailResponse toDetail(Listing listing, List<ListingMedia> media) {
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
                .thumbnailUrl(resolveThumbnailUrl(media))
                .media(toMediaResponses(media))
                .poster(ListingDetailResponse.PosterSummary.builder()
                        .userId(listing.getOwnerUser().getId())
                        .fullName(listing.getOwnerUser().getFullName())
                        .email(listing.getOwnerUser().getEmail())
                        .role(listing.getOwnerUser().getRole())
                        .build())
                .build();
    }

    private Map<String, List<ListingMedia>> getMediaByListingId(List<Listing> listings) {
        if (listings.isEmpty()) {
            return Map.of();
        }

        List<String> listingIds = listings.stream().map(Listing::getId).toList();
        Map<String, List<ListingMedia>> mediaByListingId = new HashMap<>();

        for (ListingMedia media : listingMediaRepository.findByListing_IdInOrderByListing_IdAscDisplayOrderAsc(listingIds)) {
            mediaByListingId.computeIfAbsent(media.getListing().getId(), ignored -> new java.util.ArrayList<>()).add(media);
        }

        return mediaByListingId;
    }

    private List<ListingMediaResponse> toMediaResponses(List<ListingMedia> media) {
        return media
                .stream()
                .sorted(Comparator.comparing(ListingMedia::getDisplayOrder))
                .map(this::toMedia)
                .toList();
    }

    private String resolveThumbnailUrl(List<ListingMedia> media) {
        return media
                .stream()
                .sorted(Comparator.comparing(ListingMedia::getDisplayOrder))
                .map(ListingMedia::getMediaUrl)
                .findFirst()
                .orElse(null);
    }

    private ListingMediaResponse toMedia(ListingMedia media) {
        return ListingMediaResponse.builder()
                .id(media.getId())
                .mediaType(media.getMediaType())
                .mediaUrl(media.getMediaUrl())
                .caption(media.getCaption())
                .displayOrder(media.getDisplayOrder())
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
