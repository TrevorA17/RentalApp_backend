package com.rentalapp.module.media.repository;

import com.rentalapp.module.media.entity.ListingMedia;

import java.util.List;

public interface IListingMediaRepository {
    ListingMedia save(ListingMedia media);
    List<ListingMedia> findByListing_IdOrderByDisplayOrderAsc(String listingId);
    List<ListingMedia> findByListing_IdInOrderByListing_IdAscDisplayOrderAsc(List<String> listingIds);
}
