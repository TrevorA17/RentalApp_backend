package com.rentalapp.module.media.repository;

import com.rentalapp.module.media.entity.ListingMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingMediaRepository extends JpaRepository<ListingMedia, String> {
    List<ListingMedia> findByListing_IdOrderByDisplayOrderAsc(String listingId);
    List<ListingMedia> findByListing_IdInOrderByListing_IdAscDisplayOrderAsc(List<String> listingIds);
}
