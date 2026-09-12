package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.AuthenticatedUserResource;

/**
 * Assembler converting User entity, access token, and refresh token to {@link AuthenticatedUserResource}.
 */
public class AuthenticatedUserResourceFromEntityAssembler {

    public static AuthenticatedUserResource toResourceFromEntity(User user, String token, String refreshToken) {
        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        return new AuthenticatedUserResource(
                user.getId(),
                user.getUsername().username(),
                token,
                refreshToken,
                roleNames
        );
    }
}
