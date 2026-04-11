package com.rentalapp.security;

import com.rentalapp.config.RestAccessDeniedHandler;
import com.rentalapp.config.RestAuthenticationEntryPoint;
import com.rentalapp.config.SecurityConfig;
import com.rentalapp.exception.GlobalExceptionHandler;
import com.rentalapp.module.auth.controller.AuthController;
import com.rentalapp.module.auth.service.AuthService;
import com.rentalapp.module.listings.controller.ListingController;
import com.rentalapp.module.listings.service.ListingService;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {AuthController.class, ListingController.class},
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class PublicRouteSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthService authService;

    @MockBean
    private ListingService listingService;

    @Test
    void publicListingsEndpointIsAccessibleWithoutAuthentication() throws Exception {
        when(listingService.searchPublicListings(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
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
}
