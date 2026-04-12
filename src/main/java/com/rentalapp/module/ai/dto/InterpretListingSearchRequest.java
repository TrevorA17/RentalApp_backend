package com.rentalapp.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterpretListingSearchRequest {
    @NotBlank(message = "Search text is required.")
    @Size(max = 300, message = "Search text must not exceed 300 characters.")
    private String query;
}
