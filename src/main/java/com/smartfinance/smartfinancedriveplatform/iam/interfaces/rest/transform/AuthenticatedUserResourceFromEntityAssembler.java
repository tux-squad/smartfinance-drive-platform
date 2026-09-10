package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.AuthenticatedUserResource;

/**
 * Assembler converting User entity and token string to {@link AuthenticatedUserResource}.
 */
public class AuthenticatedUserResourceFromEntityAssembler {

    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        return new AuthenticatedUserResource(
                user.getId(),
                user.getUsername().username(),
                token,
                roleNames
        );
    }
}
