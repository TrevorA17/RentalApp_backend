package com.rentalapp.module.ai.service;

import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;
import com.rentalapp.module.ai.dto.InterpretListingSearchRequest;
import com.rentalapp.module.ai.dto.InterpretListingSearchResponse;
import com.rentalapp.module.listings.entity.Amenity;

import java.util.List;

public interface AiAssistProvider {
    EnhanceListingDescriptionResponse enhanceListingDescription(EnhanceListingDescriptionRequest request);
    InterpretListingSearchResponse interpretListingSearch(InterpretListingSearchRequest request, List<Amenity> amenities);
}
