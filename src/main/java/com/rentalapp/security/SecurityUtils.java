package com.rentalapp.security;

import com.rentalapp.exception.AuthenticationException;
import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.module.auth.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static String requireCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw AuthenticationException.invalidToken();
        }

        return principal.getId();
    }

    public static String getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            return null;
        }

        return principal.getId();
    }

    public static Role requireCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw AuthenticationException.invalidToken();
        }

        return principal.getRole();
    }

    public static void requireRole(Role requiredRole) {
        Role currentRole = requireCurrentUserRole();
        if (currentRole != requiredRole) {
            throw new ForbiddenException("You do not have permission to perform this action.");
        }
    }
}
