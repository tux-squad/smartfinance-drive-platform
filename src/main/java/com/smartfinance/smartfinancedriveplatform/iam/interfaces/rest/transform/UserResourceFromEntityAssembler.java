package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UserResource;

/**
 * Assembler converting {@link User} aggregate to {@link UserResource}.
 */
public class UserResourceFromEntityAssembler {

    public static UserResource toResourceFromEntity(User user) {
        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        return new UserResource(
                user.getId(),
                user.getUsername().username(),
                roleNames
        );
    }
}
