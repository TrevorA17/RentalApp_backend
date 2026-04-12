package com.rentalapp.module.suggestions.dto;

import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuggestedListingResponse {
    private final ListingSummaryResponse listing;
    private final String reason;
    private final int score;
}
