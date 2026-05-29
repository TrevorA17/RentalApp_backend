package com.rentalapp.module.listings.repository;

import com.rentalapp.module.listings.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface IListingRepository {
    Listing save(Listing listing);
    Optional<Listing> findById(String id);
    Optional<Listing> findWithOwnerUserAndAmenitiesById(String id);
    List<Listing> findDistinctByOwnerUserIdOrderByUpdatedAtDesc(String ownerUserId);
    List<Listing> findAll();
    List<Listing> findAll(Specification<Listing> spec);
    Page<Listing> findAll(Specification<Listing> spec, Pageable pageable);
}
