package com.rentalapp.module.suggestions.service;

import com.rentalapp.module.suggestions.dto.SuggestedListingResponse;

import java.util.List;

public interface ISuggestionService {
    List<SuggestedListingResponse> getSuggestedListings(int limit);
}
