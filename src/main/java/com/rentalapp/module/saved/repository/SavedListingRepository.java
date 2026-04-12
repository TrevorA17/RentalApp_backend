package com.rentalapp.module.saved.repository;

import com.rentalapp.module.saved.entity.SavedListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedListingRepository extends JpaRepository<SavedListing, String> {
    boolean existsByUser_IdAndListing_Id(String userId, String listingId);
    Optional<SavedListing> findByUser_IdAndListing_Id(String userId, String listingId);
    List<SavedListing> findByUser_IdOrderByCreatedAtDesc(String userId);
}
