package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SignUpResource;

import java.util.List;

/**
 * Assembler converting {@link SignUpResource} to {@link SignUpCommand}.
 * Restricts public registration so that clients cannot self-assign administrative or analyst roles.
 */
public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        // Public registration strictly assigns ROLE_USER to prevent privilege escalation
        List<Roles> roles = List.of(Roles.ROLE_USER);
        return new SignUpCommand(
                new Username(resource.username()),
                new Password(resource.password()),
                roles
        );
    }
}
