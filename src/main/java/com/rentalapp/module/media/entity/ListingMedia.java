package com.rentalapp.module.media.entity;

import com.rentalapp.common.entity.BaseEntity;
import com.rentalapp.module.listings.entity.Listing;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "listing_media", indexes = {
        @Index(name = "idx_listing_media_listing_id", columnList = "listing_id"),
        @Index(name = "idx_listing_media_display_order", columnList = "listing_id,display_order")
})
public class ListingMedia extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MediaType mediaType;

    @Column(nullable = false, length = 1000)
    private String mediaUrl;

    @Column(length = 255)
    private String caption;

    @Column(nullable = false)
    private Integer displayOrder;
}
