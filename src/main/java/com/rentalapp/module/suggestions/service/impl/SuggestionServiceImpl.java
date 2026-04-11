package com.rentalapp.module.suggestions.service.impl;

import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.inquiries.repository.InquiryRepository;
import com.rentalapp.module.listings.dto.AmenityResponse;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.module.media.dto.ListingMediaResponse;
import com.rentalapp.module.media.entity.ListingMedia;
import com.rentalapp.module.media.repository.ListingMediaRepository;
import com.rentalapp.module.profiles.entity.Profile;
import com.rentalapp.module.profiles.repository.ProfileRepository;
import com.rentalapp.module.saved.repository.SavedListingRepository;
import com.rentalapp.module.suggestions.dto.SuggestedListingResponse;
import com.rentalapp.module.suggestions.service.SuggestionService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {
    private final ListingRepository listingRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final ProfileRepository profileRepository;
    private final SavedListingRepository savedListingRepository;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SuggestedListingResponse> getSuggestedListings(int limit) {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Admins do not receive listing suggestions.");
        }

        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        List<Listing> savedListings = savedListingRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(savedListing -> savedListing.getListing())
                .toList();
        List<Listing> inquiryListings = inquiryRepository.findBySenderUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(inquiry -> inquiry.getListing())
                .toList();

        Set<String> excludedListingIds = new HashSet<>();
        excludedListingIds.addAll(savedListings.stream().map(Listing::getId).toList());
        excludedListingIds.addAll(inquiryListings.stream().map(Listing::getId).toList());

        Set<String> preferredCities = new HashSet<>();
        Set<String> preferredAreas = new HashSet<>();
        Set<String> preferredHouseTypes = new HashSet<>();
        Set<String> preferredAmenities = new HashSet<>();

        if (profile != null) {
            if (profile.getCity() != null && !profile.getCity().isBlank()) {
                preferredCities.add(profile.getCity().trim().toLowerCase(Locale.ROOT));
            }

            parseCsv(profile.getServiceAreas()).stream()
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .forEach(preferredAreas::add);
        }

        for (Listing listing : savedListings) {
            addPreferencesFromListing(listing, preferredCities, preferredAreas, preferredHouseTypes, preferredAmenities);
        }

        for (Listing listing : inquiryListings) {
            addPreferencesFromListing(listing, preferredCities, preferredAreas, preferredHouseTypes, preferredAmenities);
        }

        List<Listing> candidates = listingRepository.findAll((root, query, cb) -> cb.and(
                        cb.equal(root.get("listingStatus"), ListingStatus.PUBLISHED),
                        cb.equal(root.get("approvalStatus"), ApprovalStatus.APPROVED)))
                .stream()
                .filter(listing -> !listing.getOwnerUser().getId().equals(userId))
                .filter(listing -> !excludedListingIds.contains(listing.getId()))
                .toList();

        Map<String, List<ListingMedia>> mediaByListingId = getMediaByListingId(candidates);

        return candidates.stream()
                .map(listing -> scoreListing(
                        listing,
                        mediaByListingId.getOrDefault(listing.getId(), List.of()),
                        preferredCities,
                        preferredAreas,
                        preferredHouseTypes,
                        preferredAmenities
                ))
                .sorted(Comparator.comparingInt(ScoredSuggestion::score).reversed()
                        .thenComparing(ScoredSuggestion::publishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(1, Math.min(limit, 12)))
                .map(scored -> SuggestedListingResponse.builder()
                        .listing(scored.listing())
                        .reason(scored.reason())
                        .score(scored.score())
                        .build())
                .toList();
    }

    private void addPreferencesFromListing(
            Listing listing,
            Set<String> preferredCities,
            Set<String> preferredAreas,
            Set<String> preferredHouseTypes,
            Set<String> preferredAmenities
    ) {
        if (listing.getCity() != null && !listing.getCity().isBlank()) {
            preferredCities.add(listing.getCity().trim().toLowerCase(Locale.ROOT));
        }

        if (listing.getArea() != null && !listing.getArea().isBlank()) {
            preferredAreas.add(listing.getArea().trim().toLowerCase(Locale.ROOT));
        }

        if (listing.getHouseType() != null) {
            preferredHouseTypes.add(listing.getHouseType().name());
        }

        listing.getAmenities().forEach(amenity -> preferredAmenities.add(amenity.getId()));
    }

    private ScoredSuggestion scoreListing(
            Listing listing,
            List<ListingMedia> media,
            Set<String> preferredCities,
            Set<String> preferredAreas,
            Set<String> preferredHouseTypes,
            Set<String> preferredAmenities
    ) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        String city = normalize(listing.getCity());
        String area = normalize(listing.getArea());

        if (city != null && preferredCities.contains(city)) {
            score += 50;
            reasons.add("matches your city");
        }

        if (area != null && preferredAreas.contains(area)) {
            score += 35;
            reasons.add("matches an area you seem to prefer");
        }

        if (listing.getHouseType() != null && preferredHouseTypes.contains(listing.getHouseType().name())) {
            score += 20;
            reasons.add("matches your preferred property type");
        }

        long matchingAmenities = listing.getAmenities().stream()
                .map(amenity -> amenity.getId())
                .filter(preferredAmenities::contains)
                .count();
        if (matchingAmenities > 0) {
            score += (int) Math.min(20, matchingAmenities * 5);
            reasons.add("shares amenities from your saved or viewed activity");
        }

        if (media.isEmpty()) {
            score -= 5;
        } else {
            score += Math.min(10, media.size() * 2);
        }

        if (listing.getPublishedAt() != null) {
            long ageDays = Math.max(0, Duration.between(listing.getPublishedAt(), Instant.now()).toDays());
            score += Math.max(0, 20 - (int) ageDays);
        }

        if (reasons.isEmpty()) {
            reasons.add("fresh published listing");
        }

        return new ScoredSuggestion(toSummary(listing, media), String.join(", ", reasons), score, listing.getPublishedAt());
    }

    private Map<String, List<ListingMedia>> getMediaByListingId(List<Listing> listings) {
        if (listings.isEmpty()) {
            return Map.of();
        }

        List<String> listingIds = listings.stream().map(Listing::getId).toList();
        Map<String, List<ListingMedia>> mediaByListingId = new HashMap<>();

        for (ListingMedia media : listingMediaRepository.findByListing_IdInOrderByListing_IdAscDisplayOrderAsc(listingIds)) {
            mediaByListingId.computeIfAbsent(media.getListing().getId(), ignored -> new ArrayList<>()).add(media);
        }

        return mediaByListingId;
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
                .amenities(listing.getAmenities().stream()
                        .map(amenity -> AmenityResponse.builder()
                                .id(amenity.getId())
                                .name(amenity.getName())
                                .slug(amenity.getSlug())
                                .build())
                        .toList())
                .thumbnailUrl(media.stream().min(Comparator.comparing(ListingMedia::getDisplayOrder)).map(ListingMedia::getMediaUrl).orElse(null))
                .media(media.stream()
                        .sorted(Comparator.comparing(ListingMedia::getDisplayOrder))
                        .map(item -> ListingMediaResponse.builder()
                                .id(item.getId())
                                .mediaType(item.getMediaType())
                                .mediaUrl(item.getMediaUrl())
                                .caption(item.getCaption())
                                .displayOrder(item.getDisplayOrder())
                                .build())
                        .toList())
                .build();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> parseCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return List.of(value.split(",")).stream()
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private record ScoredSuggestion(ListingSummaryResponse listing, String reason, int score, Instant publishedAt) {
    }
}
