package com.rentalapp.module.inquiries.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.inquiries.dto.CreateInquiryRequest;
import com.rentalapp.module.inquiries.dto.InquiryResponse;
import com.rentalapp.module.inquiries.dto.UpdateInquiryStatusRequest;
import com.rentalapp.module.inquiries.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;

    @PostMapping("/api/v1/listings/{listingId}/inquiries")
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(
            @PathVariable String listingId,
            @Valid @RequestBody CreateInquiryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Inquiry sent successfully.", inquiryService.createInquiry(listingId, request)));
    }

    @GetMapping("/api/v1/inquiries/sent")
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getSentInquiries() {
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.getSentInquiries()));
    }

    @GetMapping("/api/v1/inquiries/received")
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getReceivedInquiries() {
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.getReceivedInquiries()));
    }

    @PatchMapping("/api/v1/inquiries/{inquiryId}/status")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateInquiryStatus(
            @PathVariable String inquiryId,
            @Valid @RequestBody UpdateInquiryStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Inquiry status updated successfully.", inquiryService.updateInquiryStatus(inquiryId, request)));
    }
}
