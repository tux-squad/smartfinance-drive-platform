package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SignUpResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembler converting {@link SignUpResource} to {@link SignUpCommand}.
 */
public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        List<Roles> roles = new ArrayList<>();
        if (resource.roles() != null) {
            for (String roleStr : resource.roles()) {
                try {
                    roles.add(Roles.valueOf(roleStr));
                } catch (IllegalArgumentException ignored) {
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
