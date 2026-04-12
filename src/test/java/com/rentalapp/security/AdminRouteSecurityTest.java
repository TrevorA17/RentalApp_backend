package com.rentalapp.security;

import com.rentalapp.config.RestAccessDeniedHandler;
import com.rentalapp.config.RestAuthenticationEntryPoint;
import com.rentalapp.config.SecurityConfig;
import com.rentalapp.exception.GlobalExceptionHandler;
import com.rentalapp.module.admin.controller.AdminModerationController;
import com.rentalapp.module.admin.repository.ModerationActionRepository;
import com.rentalapp.module.admin.service.ModerationAuditService;
import com.rentalapp.module.admin.service.impl.AdminModerationServiceImpl;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.entity.UserStatus;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.repository.ListingRepository;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminModerationController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class, GlobalExceptionHandler.class, AdminModerationServiceImpl.class, JwtAuthenticationFilter.class})
class AdminRouteSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ListingRepository listingRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ModerationAuditService moderationAuditService;

    @MockBean
    private ModerationActionRepository moderationActionRepository;

    @Test
    void adminUsersEndpointRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    void adminUsersEndpointRejectsNonAdminUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(authentication(buildAuthentication("user-1", Role.RENTER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminUsersEndpointAllowsAdminUsers() throws Exception {
        User admin = new User();
        admin.setId("admin-1");
        admin.setFullName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);

        when(userRepository.findAll()).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(authentication(buildAuthentication("admin-1", Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("admin@example.com"));
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(String userId, Role role) {
        AuthUserPrincipal principal = AuthUserPrincipal.builder()
                .id(userId)
                .email(userId + "@example.com")
                .role(role)
                .tokenId("token-id")
                .tokenType("ACCESS")
                .build();

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
