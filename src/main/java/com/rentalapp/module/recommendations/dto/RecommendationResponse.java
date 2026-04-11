package com.rentalapp.module.recommendations.dto;

import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationResponse {
    private final ListingSummaryResponse listing;
    private final String reason;
    private final int score;
}
