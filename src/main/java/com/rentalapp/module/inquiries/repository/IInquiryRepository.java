package com.rentalapp.module.inquiries.repository;

import com.rentalapp.module.inquiries.entity.Inquiry;

import java.util.List;
import java.util.Optional;

public interface IInquiryRepository {
    Inquiry save(Inquiry inquiry);
    Optional<Inquiry> findById(String inquiryId);
    List<Inquiry> findBySenderUser_IdOrderByCreatedAtDesc(String senderUserId);
    List<Inquiry> findByRecipientUser_IdOrderByCreatedAtDesc(String recipientUserId);
}
