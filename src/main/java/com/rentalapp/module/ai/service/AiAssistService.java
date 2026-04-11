package com.rentalapp.module.ai.service;

import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;

public interface AiAssistService {
    EnhanceListingDescriptionResponse enhanceListingDescription(EnhanceListingDescriptionRequest request);
}
