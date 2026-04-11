package com.rentalapp.module.listings.controller;

import com.rentalapp.common.api.PaginatedResponse;
import com.rentalapp.exception.GlobalExceptionHandler;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.entity.ApprovalStatus;
import com.rentalapp.module.listings.entity.AvailabilityStatus;
import com.rentalapp.module.listings.entity.HouseType;
import com.rentalapp.module.listings.entity.ListingStatus;
import com.rentalapp.module.listings.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ListingControllerTest {
    @Mock
    private ListingService listingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ListingController(listingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchListingsReturnsPublishedResults() throws Exception {
        ListingSummaryResponse listing = ListingSummaryResponse.builder()
                .id("listing-1")
                .title("Sunny Kilimani Apartment")
                .rentAmount(new BigDecimal("35000"))
                .city("Nairobi")
                .area("Kilimani")
                .bedrooms(1)
                .bathrooms(1)
                .houseType(HouseType.APARTMENT)
                .furnished(true)
                .availabilityStatus(AvailabilityStatus.AVAILABLE_NOW)
                .listingStatus(ListingStatus.PUBLISHED)
                .approvalStatus(ApprovalStatus.APPROVED)
                .ownerType(Role.AGENT)
                .amenities(List.of())
                .media(List.of())
                .build();

        PaginatedResponse<ListingSummaryResponse> response = PaginatedResponse.<ListingSummaryResponse>builder()
                .items(List.of(listing))
                .page(0)
                .size(12)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .sort("PUBLISHED_AT_DESC")
                .build();

        when(listingService.searchPublicListings(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/listings")
                        .param("city", "Nairobi")
                        .param("bedrooms", "1")
                        .param("page", "0")
                        .param("size", "12")
                        .param("sort", "PUBLISHED_AT_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].title").value("Sunny Kilimani Apartment"))
                .andExpect(jsonPath("$.data.items[0].listingStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.sort").value("PUBLISHED_AT_DESC"));
    }
}
