package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import java.util.List;

/**
 * REST Request resource for user sign-up registration.
 */
public record SignUpResource(
        String username,
        String password,
        List<String> roles
) {
}
