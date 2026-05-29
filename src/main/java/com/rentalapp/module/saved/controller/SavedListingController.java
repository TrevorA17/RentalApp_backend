package com.rentalapp.module.saved.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.saved.service.ISavedListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SAVED_LISTING_MANAGE')")
public class SavedListingController {
    private final ISavedListingService savedListingService;

    @PostMapping("/api/v1/listings/{listingId}/save")
    public ResponseEntity<ApiResponse<Void>> saveListing(@PathVariable String listingId) {
        savedListingService.saveListing(listingId);
        return ResponseEntity.ok(ApiResponse.ok("Listing saved successfully.", null));
    }

    @DeleteMapping("/api/v1/listings/{listingId}/save")
    public ResponseEntity<ApiResponse<Void>> removeSavedListing(@PathVariable String listingId) {
        savedListingService.removeSavedListing(listingId);
        return ResponseEntity.ok(ApiResponse.ok("Listing removed from saved listings.", null));
    }

    @GetMapping("/api/v1/saved-listings")
    public ResponseEntity<ApiResponse<List<ListingSummaryResponse>>> getSavedListings() {
        return ResponseEntity.ok(ApiResponse.ok(savedListingService.getSavedListings()));
    }

    @GetMapping("/api/v1/saved-listings/ids")
    public ResponseEntity<ApiResponse<List<String>>> getSavedListingIds() {
        return ResponseEntity.ok(ApiResponse.ok(savedListingService.getSavedListingIds()));
    }
}
