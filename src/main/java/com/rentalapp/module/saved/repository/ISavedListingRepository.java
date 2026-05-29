package com.rentalapp.module.saved.repository;

import com.rentalapp.module.saved.entity.SavedListing;

import java.util.List;
import java.util.Optional;

public interface ISavedListingRepository {
    SavedListing save(SavedListing savedListing);
    void delete(SavedListing savedListing);
    boolean existsByUser_IdAndListing_Id(String userId, String listingId);
    Optional<SavedListing> findByUser_IdAndListing_Id(String userId, String listingId);
    List<SavedListing> findByUser_IdOrderByCreatedAtDesc(String userId);
}
