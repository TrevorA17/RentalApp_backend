package com.rentalapp.module.listings.repository;

import com.rentalapp.module.listings.entity.Listing;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, String>, JpaSpecificationExecutor<Listing> {
    @EntityGraph(attributePaths = {"ownerUser", "amenities"})
    Optional<Listing> findWithOwnerUserAndAmenitiesById(String id);

    @EntityGraph(attributePaths = {"amenities"})
    List<Listing> findDistinctByOwnerUserIdOrderByUpdatedAtDesc(String ownerUserId);

    @Override
    @EntityGraph(attributePaths = {"ownerUser", "amenities"})
    List<Listing> findAll(org.springframework.data.jpa.domain.Specification<Listing> spec);

    @Override
    @EntityGraph(attributePaths = {"ownerUser", "amenities"})
    Page<Listing> findAll(org.springframework.data.jpa.domain.Specification<Listing> spec, Pageable pageable);
}
