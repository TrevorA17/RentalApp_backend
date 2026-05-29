package com.rentalapp.module.saved.repository;

import com.rentalapp.module.saved.entity.SavedListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedListingRepository extends JpaRepository<SavedListing, String>, ISavedListingRepository {
}
