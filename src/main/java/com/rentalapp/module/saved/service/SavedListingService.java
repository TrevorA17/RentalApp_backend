package com.rentalapp.module.saved.service;

import com.rentalapp.module.listings.dto.ListingSummaryResponse;

import java.util.List;

public interface SavedListingService {
    void saveListing(String listingId);
    void removeSavedListing(String listingId);
    List<ListingSummaryResponse> getSavedListings();
    List<String> getSavedListingIds();
}
