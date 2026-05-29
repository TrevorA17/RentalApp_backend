package com.rentalapp.security;

import com.rentalapp.config.RestAccessDeniedHandler;
import com.rentalapp.config.RestAuthenticationEntryPoint;
import com.rentalapp.common.api.PaginatedResponse;
import com.rentalapp.config.SecurityConfig;
import com.rentalapp.exception.GlobalExceptionHandler;
import com.rentalapp.module.ai.controller.AiAssistController;
import com.rentalapp.module.ai.dto.InterpretListingSearchResponse;
import com.rentalapp.module.ai.service.IAiAssistService;
import com.rentalapp.module.auth.controller.AuthController;
import com.rentalapp.module.auth.service.IAuthService;
import com.rentalapp.module.auth.service.impl.AuthRateLimitService;
import com.rentalapp.module.listings.controller.ListingController;
import com.rentalapp.module.listings.dto.ListingSummaryResponse;
import com.rentalapp.module.listings.service.IListingService;
import com.rentalapp.module.media.service.IListingMediaUploadService;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {AuthController.class, ListingController.class, AiAssistController.class},
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class PublicRouteSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private IAuthService authService;

    @MockBean
    private AuthRateLimitService authRateLimitService;

    @MockBean
    private IListingService listingService;

    @MockBean
    private IListingMediaUploadService listingMediaUploadService;

    @MockBean
    private IAiAssistService aiAssistService;

    @Test
    void publicListingsEndpointIsAccessibleWithoutAuthentication() throws Exception {
        when(listingService.searchPublicListings(any())).thenReturn(PaginatedResponse.<ListingSummaryResponse>builder()
                .items(java.util.List.of())
                .currentPage(1)
                .perPage(10)
                .totalItems(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .sort("PUBLISHED_AT_DESC")
                .build());

        mockMvc.perform(get("/api/v1/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void protectedListingCreationRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(post("/api/v1/listings")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test listing"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    void authMeRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    void aiSearchInterpretEndpointIsAccessibleWithoutAuthentication() throws Exception {
        when(aiAssistService.interpretListingSearch(any())).thenReturn(InterpretListingSearchResponse.builder()
                .normalizedQuery("2 bedroom in Kilimani under 50k")
                .interpreted(true)
                .provider("heuristic-fallback")
                .matchedSignals(java.util.List.of("bedrooms", "area", "maxPrice"))
                .notes(java.util.List.of())
                .filters(InterpretListingSearchResponse.Filters.builder().amenities(java.util.List.of()).build())
                .build());

        mockMvc.perform(post("/api/v1/ai/search/interpret")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "2 bedroom in Kilimani under 50k"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.provider").value("heuristic-fallback"));
    }
}
