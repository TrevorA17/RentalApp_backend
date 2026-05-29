package com.rentalapp.module.inquiries.service;

import com.rentalapp.module.inquiries.dto.CreateInquiryRequest;
import com.rentalapp.module.inquiries.dto.InquiryResponse;
import com.rentalapp.module.inquiries.dto.UpdateInquiryStatusRequest;

import java.util.List;

public interface IInquiryService {
    InquiryResponse createInquiry(String listingId, CreateInquiryRequest request);
    List<InquiryResponse> getSentInquiries();
    List<InquiryResponse> getReceivedInquiries();
    InquiryResponse updateInquiryStatus(String inquiryId, UpdateInquiryStatusRequest request);
}
