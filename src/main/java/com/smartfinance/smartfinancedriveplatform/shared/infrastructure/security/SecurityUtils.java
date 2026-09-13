package com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utility helper for extracting authenticated user details from Spring SecurityContext.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Obtains the authenticated username (email) from SecurityContextHolder.
     *
     * @return Optional containing the username if authenticated, empty otherwise.
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }

    /**
     * Obtains the authenticated user ID if stored in authentication details or principal.
     *
     * @return Optional containing the userId as String if present, empty otherwise.
     */
    public static Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        if (authentication.getDetails() instanceof AuthenticatedUserDetails details) {
            return Optional.ofNullable(details.userId());
        }
        return Optional.empty();
    }

    /**
     * Record holding authenticated user context details.
     */
    public record AuthenticatedUserDetails(String userId, String username) {}
}
