package com.rentalapp.module.listings.service;

import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.dto.ListingUpsertRequest;
import com.rentalapp.module.listings.entity.Amenity;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.AvailabilityStatus;
import com.rentalapp.module.listings.entity.HouseType;
import com.rentalapp.module.listings.entity.Listing;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.repository.AmenityRepository;
import com.rentalapp.module.listings.repository.ListingRepository;
import com.rentalapp.module.listings.service.impl.ListingService;
import com.rentalapp.module.media.repository.ListingMediaRepository;
import com.rentalapp.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {
    @Mock
    private ListingRepository listingRepository;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private ListingMediaRepository listingMediaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListingService listingService;

    @Test
    void createListingRejectsRenterAccounts() {
        User renter = buildUser("user-1", Role.RENTER);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("user-1");
            when(userRepository.findById("user-1")).thenReturn(Optional.of(renter));

            assertThrows(ForbiddenException.class, () -> listingService.createListing(buildRequest()));
        }
    }

    @Test
    void publishListingRequiresAmenitiesAndDescription() {
        User owner = buildUser("owner-1", Role.AGENT);
        Listing listing = new Listing();
        listing.setId("listing-1");
        listing.setOwnerUser(owner);
        listing.setOwnerType(Role.AGENT);
        listing.setTitle("Untidy draft");
        listing.setDescription("Draft");
        listing.setAmenities(new LinkedHashSet<>());
        listing.setListingStatus(ListingStatus.DRAFT);
        listing.setApprovalStatus(ApprovalStatus.PENDING);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("owner-1");
            when(userRepository.findById("owner-1")).thenReturn(Optional.of(owner));
            when(listingRepository.findWithOwnerUserAndAmenitiesById("listing-1")).thenReturn(Optional.of(listing));

            assertThrows(ValidationException.class, () -> listingService.publishListing("listing-1"));
        }
    }

    @Test
    void createListingPersistsNormalizedValues() {
        User owner = buildUser("owner-1", Role.LANDLORD);
        Amenity amenity = new Amenity();
        amenity.setId("amenity-1");
        amenity.setName("Parking");
        amenity.setSlug("parking");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUserId).thenReturn("owner-1");
            when(userRepository.findById("owner-1")).thenReturn(Optional.of(owner));
            when(amenityRepository.findByIdIn(List.of("amenity-1"))).thenReturn(List.of(amenity));
            when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = listingService.createListing(buildRequest());

            assertEquals("Bright studio", response.getTitle());
            assertEquals("Nairobi", response.getCity());
            assertEquals(1, response.getAmenities().size());
        }
    }

    private User buildUser(String id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setFullName("Owner");
        user.setEmail("owner@example.com");
        return user;
    }

    private ListingUpsertRequest buildRequest() {
        ListingUpsertRequest request = new ListingUpsertRequest();
        request.setTitle("  Bright studio  ");
        request.setDescription("  Clean and ready  ");
        request.setRentAmount(new BigDecimal("30000"));
        request.setDepositAmount(new BigDecimal("30000"));
        request.setAgentFeeAmount(BigDecimal.ZERO);
        request.setCity(" Nairobi ");
        request.setArea(" Kilimani ");
        request.setBedrooms(1);
        request.setBathrooms(1);
        request.setHouseType(HouseType.STUDIO);
        request.setFurnished(true);
        request.setAvailabilityStatus(AvailabilityStatus.AVAILABLE_NOW);
        request.setAmenityIds(List.of("amenity-1"));
        request.setMedia(List.of());
        return request;
    }
}
