package com.rentalapp.module.inquiries.dto;

import com.rentalapp.module.inquiries.entity.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInquiryStatusRequest {
    @NotNull(message = "Status is required.")
    private InquiryStatus status;
}
