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
        // Assign ROLE_USER by default. Allow ROLE_DEALER if explicitly requested, but disallow ROLE_ADMIN to prevent privilege escalation.
        List<Roles> roles = new java.util.ArrayList<>();
        roles.add(Roles.ROLE_USER);
        if (resource.roles() != null) {
            for (String r : resource.roles()) {
                if ("ROLE_DEALER".equalsIgnoreCase(r) || "DEALER".equalsIgnoreCase(r)) {
                    if (!roles.contains(Roles.ROLE_DEALER)) {
                        roles.add(Roles.ROLE_DEALER);
                    }
                }
            }
        }
        return new SignUpCommand(
                new Username(resource.username()),
                new Password(resource.password()),
                roles
        );
    }
}
