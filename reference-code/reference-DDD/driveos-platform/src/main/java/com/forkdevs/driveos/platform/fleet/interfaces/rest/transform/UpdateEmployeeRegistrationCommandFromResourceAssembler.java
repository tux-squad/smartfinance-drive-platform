package com.forkdevs.driveos.platform.fleet.interfaces.rest.transform;

import com.forkdevs.driveos.platform.core.domain.model.valueobjects.EmployeeId;
import com.forkdevs.driveos.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.forkdevs.driveos.platform.fleet.interfaces.rest.resources.UpdateEmployeeRegistrationResource;

import java.util.UUID;

public class UpdateEmployeeRegistrationCommandFromResourceAssembler {
    public static UpdateEmployeeRegistrationCommand toCommandFromResource(UUID id, UpdateEmployeeRegistrationResource resource) {
        return new UpdateEmployeeRegistrationCommand(
                new EmployeeId(id),
                resource.speciality(),
                resource.specialityName(),
                resource.salary()
        );
    }
}
