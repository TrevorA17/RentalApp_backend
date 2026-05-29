package com.rentalapp.module.inquiries.repository;

import com.rentalapp.module.inquiries.entity.Inquiry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, String>, IInquiryRepository {
    @Override
    @EntityGraph(attributePaths = {"listing", "senderUser", "recipientUser"})
    List<Inquiry> findBySenderUser_IdOrderByCreatedAtDesc(String senderUserId);

    @Override
    @EntityGraph(attributePaths = {"listing", "senderUser", "recipientUser"})
    List<Inquiry> findByRecipientUser_IdOrderByCreatedAtDesc(String recipientUserId);

    @Override
    @EntityGraph(attributePaths = {"listing", "senderUser", "recipientUser"})
    Optional<Inquiry> findById(String inquiryId);
}
