package com.rentalapp.module.listings.service;

import com.rentalapp.module.listings.dto.AmenityResponse;
import com.rentalapp.module.listings.dto.ListingDetailResponse;
import com.rentalapp.module.listings.dto.ListingSearchRequest;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.dto.ListingUpsertRequest;

import java.util.List;

public interface ListingService {
    ListingSummaryResponse createListing(ListingUpsertRequest request);
    ListingSummaryResponse updateListing(String listingId, ListingUpsertRequest request);
    ListingSummaryResponse publishListing(String listingId);
    List<ListingSummaryResponse> getMyListings();
    ListingDetailResponse getListingById(String listingId);
    List<ListingSummaryResponse> searchPublicListings(ListingSearchRequest request);
    List<AmenityResponse> getAmenities();
}
