package com.rentalapp.module.listings.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AmenityResponse {
    private final String id;
    private final String name;
    private final String slug;
}
