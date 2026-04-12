package com.rentalapp.module.ai.service;

import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;
import com.rentalapp.module.ai.dto.InterpretListingSearchRequest;
import com.rentalapp.module.ai.dto.InterpretListingSearchResponse;

public interface AiAssistService {
    EnhanceListingDescriptionResponse enhanceListingDescription(EnhanceListingDescriptionRequest request);
    InterpretListingSearchResponse interpretListingSearch(InterpretListingSearchRequest request);
}
