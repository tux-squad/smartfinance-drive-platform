package com.forkdevs.driveos.platform.iam.interfaces.rest.transform;

import com.forkdevs.driveos.platform.iam.domain.model.commands.GeneratePasswordRecoveryTokenCommand;
import com.forkdevs.driveos.platform.iam.interfaces.rest.resources.PasswordRecoveryResource;

public class GeneratePasswordRecoveryTokenCommandFromResourceAssembler {
    public static GeneratePasswordRecoveryTokenCommand toCommandFromResource(PasswordRecoveryResource resource) {
        return new GeneratePasswordRecoveryTokenCommand(new com.forkdevs.driveos.platform.iam.domain.model.valueobjects.EmailAddress(resource.email()));
    }
}
