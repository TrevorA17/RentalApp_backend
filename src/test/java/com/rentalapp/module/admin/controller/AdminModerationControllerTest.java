package com.rentalapp.module.admin.controller;

import com.rentalapp.exception.GlobalExceptionHandler;
import com.rentalapp.module.admin.dto.AdminUserResponse;
import com.rentalapp.module.admin.service.IAdminModerationService;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminModerationControllerTest {
    @Mock
    private IAdminModerationService adminModerationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminModerationController(adminModerationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUsersReturnsModerationList() throws Exception {
        AdminUserResponse user = AdminUserResponse.builder()
                .id("user-1")
                .fullName("Admin User")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        when(adminModerationService.getUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }
}
