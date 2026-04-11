package com.rentalapp.module.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EnhanceListingDescriptionResponse {
    private final String enhancedDescription;
    private final List<String> suggestions;
    private final String provider;
}
