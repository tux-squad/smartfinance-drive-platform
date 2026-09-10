package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import java.util.List;

/**
 * REST Response resource representing User details.
 */
public record UserResource(
        Long id,
        String username,
        List<String> roles
) {
}
