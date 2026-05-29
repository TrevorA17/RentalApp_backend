package com.rentalapp.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterpretListingSearchRequest {
    @NotBlank(message = "Search text is required.")
    @Size(max = 300, message = "Search text must not exceed 300 characters.")
    private String query;
}
