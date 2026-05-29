package com.rentalapp.module.media.repository;

import com.rentalapp.module.media.entity.ListingMedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingMediaRepository extends JpaRepository<ListingMedia, String>, IListingMediaRepository {
}
