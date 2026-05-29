package com.rentalapp.module.inquiries.dto;

import com.rentalapp.module.inquiries.entity.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInquiryStatusRequest {
    @NotNull(message = "Status is required.")
    private InquiryStatus status;
}
