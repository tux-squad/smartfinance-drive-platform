package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import java.util.List;

/**
 * REST Response resource returned upon successful authentication.
 */
public record AuthenticatedUserResource(
        Long id,
        String username,
        String token,
        String refreshToken,
        List<String> roles
) {
}
