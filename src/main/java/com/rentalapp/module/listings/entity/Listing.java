package com.rentalapp.module.listings.entity;

import com.rentalapp.common.entity.BaseEntity;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "listings")
public class Listing extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal rentAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal agentFeeAmount;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 150)
    private String area;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(nullable = false)
    private Integer bathrooms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HouseType houseType;

    @Column(nullable = false)
    private boolean furnished;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AvailabilityStatus availabilityStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ListingStatus listingStatus = ListingStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role ownerType;

    private Instant publishedAt;
    private Instant archivedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "listing_amenities",
            joinColumns = @JoinColumn(name = "listing_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new LinkedHashSet<>();
}
