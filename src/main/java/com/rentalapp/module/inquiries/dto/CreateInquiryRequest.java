package com.rentalapp.module.inquiries.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInquiryRequest {
    @NotBlank(message = "Message is required.")
    @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters.")
    private String message;
}
