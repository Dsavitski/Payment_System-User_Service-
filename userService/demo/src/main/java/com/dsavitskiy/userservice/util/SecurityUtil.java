package com.dsavitskiy.userservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public class SecurityUtil {

    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return TEST_USER_ID;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object credentials = jwtAuth.getCredentials();
            if (credentials instanceof Jwt jwt) {
                String userId = jwt.getClaimAsString("sub");
                if (userId != null) {
                    try {
                        return UUID.fromString(userId);
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }

        return TEST_USER_ID;
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}