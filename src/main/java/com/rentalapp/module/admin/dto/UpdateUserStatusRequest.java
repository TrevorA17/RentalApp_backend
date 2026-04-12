package com.rentalapp.module.admin.dto;

import com.rentalapp.module.auth.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {
    @NotNull(message = "User status is required.")
    private UserStatus status;
}
