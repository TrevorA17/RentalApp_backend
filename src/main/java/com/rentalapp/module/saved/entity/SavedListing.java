package com.rentalapp.module.saved.entity;

import com.rentalapp.common.entity.BaseEntity;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.listings.entity.Listing;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "saved_listings", indexes = {
        @Index(name = "idx_saved_listings_user_id", columnList = "user_id"),
        @Index(name = "idx_saved_listings_listing_id", columnList = "listing_id"),
        @Index(name = "idx_saved_listings_user_listing", columnList = "user_id,listing_id", unique = true)
})
public class SavedListing extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
}
