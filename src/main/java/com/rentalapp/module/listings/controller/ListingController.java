package com.rentalapp.module.listings.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.listings.dto.AmenityResponse;
import com.rentalapp.module.listings.dto.ListingDetailResponse;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.dto.ListingUpsertRequest;
import com.rentalapp.module.listings.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ListingController {
    private final ListingService listingService;

    @PostMapping("/api/v1/listings")
    public ResponseEntity<ApiResponse<ListingSummaryResponse>> createListing(@Valid @RequestBody ListingUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Listing created successfully", listingService.createListing(request)));
    }

    @PutMapping("/api/v1/listings/{listingId}")
    public ResponseEntity<ApiResponse<ListingSummaryResponse>> updateListing(
            @PathVariable String listingId,
            @Valid @RequestBody ListingUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Listing updated successfully", listingService.updateListing(listingId, request)));
    }

    @PostMapping("/api/v1/listings/{listingId}/publish")
    public ResponseEntity<ApiResponse<ListingSummaryResponse>> publishListing(@PathVariable String listingId) {
        return ResponseEntity.ok(ApiResponse.ok("Listing published successfully", listingService.publishListing(listingId)));
    }

    @GetMapping("/api/v1/listings/{listingId}")
    public ResponseEntity<ApiResponse<ListingDetailResponse>> getListing(@PathVariable String listingId) {
        return ResponseEntity.ok(ApiResponse.ok(listingService.getListingById(listingId)));
    }

    @GetMapping("/api/v1/my/listings")
    public ResponseEntity<ApiResponse<List<ListingSummaryResponse>>> getMyListings() {
        return ResponseEntity.ok(ApiResponse.ok(listingService.getMyListings()));
    }

    @GetMapping("/api/v1/amenities")
    public ResponseEntity<ApiResponse<List<AmenityResponse>>> getAmenities() {
        return ResponseEntity.ok(ApiResponse.ok(listingService.getAmenities()));
    }
}
