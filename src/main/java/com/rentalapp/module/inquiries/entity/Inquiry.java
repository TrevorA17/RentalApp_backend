package com.rentalapp.module.inquiries.entity;

import com.rentalapp.common.entity.BaseEntity;
import com.rentalapp.module.auth.entity.User;
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
@Table(name = "inquiries", indexes = {
        @Index(name = "idx_inquiries_listing_id", columnList = "listing_id"),
        @Index(name = "idx_inquiries_sender_user_id", columnList = "sender_user_id"),
        @Index(name = "idx_inquiries_recipient_user_id", columnList = "recipient_user_id"),
        @Index(name = "idx_inquiries_status", columnList = "status")
})
public class Inquiry extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User senderUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InquiryStatus status = InquiryStatus.NEW;
}
