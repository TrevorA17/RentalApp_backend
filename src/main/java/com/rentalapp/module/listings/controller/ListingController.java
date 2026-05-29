package com.rentalapp.module.listings.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.common.api.PaginatedResponse;
import com.rentalapp.module.listings.dto.AmenityResponse;
import com.rentalapp.module.listings.dto.ListingDetailResponse;
import com.rentalapp.module.listings.dto.ListingSearchRequest;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.dto.ListingSortOption;
import com.rentalapp.module.listings.dto.ListingUpsertRequest;
import com.rentalapp.module.listings.service.IListingService;
import com.rentalapp.module.media.dto.MediaUploadResponse;
import com.rentalapp.module.media.service.IListingMediaUploadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class ListingController {
    private final IListingService listingService;
    private final IListingMediaUploadService listingMediaUploadService;

    @GetMapping("/api/v1/listings")
    public ResponseEntity<ApiResponse<PaginatedResponse<ListingSummaryResponse>>> searchListings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Integer bathrooms,
            @RequestParam(required = false) com.rentalapp.module.listings.entity.HouseType houseType,
            @RequestParam(required = false) Boolean furnished,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer perPage,
            @RequestParam(defaultValue = "PUBLISHED_AT_DESC") ListingSortOption sort
    ) {
        ListingSearchRequest request = new ListingSearchRequest();
        request.setCity(city);
        request.setArea(area);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setBedrooms(bedrooms);
        request.setBathrooms(bathrooms);
        request.setHouseType(houseType);
        request.setFurnished(furnished);
        request.setAmenities(amenities);
        request.setPage(page);
        request.setPerPage(perPage);
        request.setSort(sort);

        return ResponseEntity.ok(ApiResponse.ok(listingService.searchPublicListings(request)));
    }

    @PostMapping("/api/v1/listings")
    public ResponseEntity<ApiResponse<ListingSummaryResponse>> createListing(@Valid @RequestBody ListingUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Listing created successfully", listingService.createListing(request)));
    }

    @PostMapping(value = "/api/v1/listings/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadListingMedia(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Listing media uploaded successfully", listingMediaUploadService.uploadListingImage(file)));
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
