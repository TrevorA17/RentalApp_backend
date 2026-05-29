package com.rentalapp.module.recommendations.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAgentRecommendationRequest {
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating cannot be greater than 5.")
    private int rating;

    @NotBlank(message = "Recommendation comment is required.")
    @Size(max = 1200, message = "Recommendation comment must not exceed 1200 characters.")
    private String comment;
}
