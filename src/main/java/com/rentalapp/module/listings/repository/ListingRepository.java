package com.rentalapp.module.listings.repository;

import com.rentalapp.module.listings.entity.Listing;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, String> {
    @EntityGraph(attributePaths = {"ownerUser", "amenities"})
    Optional<Listing> findWithOwnerUserAndAmenitiesById(String id);

    @EntityGraph(attributePaths = {"amenities"})
    List<Listing> findByOwnerUserIdOrderByUpdatedAtDesc(String ownerUserId);
}
