package com.rentalapp.module.ai.service;

import com.rentalapp.module.ai.dto.InterpretListingSearchRequest;
import com.rentalapp.module.ai.dto.InterpretListingSearchResponse;
import com.rentalapp.module.ai.service.impl.HeuristicAiAssistProvider;
import com.rentalapp.module.listings.entity.Amenity;
import com.rentalapp.module.listings.entity.HouseType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeuristicAiAssistProviderTest {
    private final HeuristicAiAssistProvider provider = new HeuristicAiAssistProvider();

    @Test
    void shouldInterpretNaturalLanguageSearchIntoStructuredFilters() {
        Amenity parking = new Amenity();
        parking.setId("amenity-parking");
        parking.setName("Parking");
        parking.setSlug("parking");

        InterpretListingSearchResponse response = provider.interpretListingSearch(
                request("2 bedroom in Kilimani under 50k with parking"),
                List.of(parking)
        );

        assertThat(response.isInterpreted()).isTrue();
        assertThat(response.getProvider()).isEqualTo("heuristic-fallback");
        assertThat(response.getFilters().getBedrooms()).isEqualTo(2);
        assertThat(response.getFilters().getArea()).isEqualTo("Kilimani");
        assertThat(response.getFilters().getMaxPrice()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(response.getFilters().getHouseType()).isNull();
        assertThat(response.getFilters().getAmenities())
                .extracting(InterpretListingSearchResponse.AmenityMatch::getName)
                .containsExactly("Parking");
    }

    @Test
    void shouldInferHouseTypeAndCityWhenSignalsArePresent() {
        InterpretListingSearchResponse response = provider.interpretListingSearch(
                request("Looking for a furnished apartment in Nairobi"),
                List.of()
        );

        assertThat(response.isInterpreted()).isTrue();
        assertThat(response.getFilters().getHouseType()).isEqualTo(HouseType.APARTMENT);
        assertThat(response.getFilters().getCity()).isEqualTo("Nairobi");
        assertThat(response.getFilters().getFurnished()).isTrue();
    }

    private InterpretListingSearchRequest request(String query) {
        InterpretListingSearchRequest request = new InterpretListingSearchRequest();
        request.setQuery(query);
        return request;
    }
}
