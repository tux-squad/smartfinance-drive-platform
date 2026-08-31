package com.smartfinance.smartfinancedriveplatform.catalog.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.catalog.application.commandservices.VehicleCommandService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.CreateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.DeleteVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.UpdateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.repositories.VehicleRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of VehicleCommandService application service.
 * Handles database transaction management and wraps validation/aggregate creation.
 */
@Service
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private final VehicleRepository vehicleRepository;

    public VehicleCommandServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public Optional<Vehicle> handle(CreateVehicleCommand command) {
        Vehicle vehicle = new Vehicle(
            command.userId(),
            command.financialEntityId(),
            command.brand(),
            command.model(),
            command.manufactureYear(),
            command.condition(),
            command.price(),
            command.imagePath()
        );
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return Optional.of(savedVehicle);
    }

    @Override
    @Transactional
    public Optional<Vehicle> handle(UpdateVehicleCommand command) {
        var vehicleOpt = vehicleRepository.findById(command.vehicleId());
        if (vehicleOpt.isEmpty()) {
            return Optional.empty();
        }
        Vehicle vehicle = vehicleOpt.get();
        vehicle.updateDetails(
            command.financialEntityId(),
            command.brand(),
            command.model(),
            command.manufactureYear(),
            command.condition(),
            command.price(),
            command.imagePath()
        );
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return Optional.of(savedVehicle);
    }

    @Override
    @Transactional
    public void handle(DeleteVehicleCommand command) {
        if (!vehicleRepository.existsById(command.vehicleId())) {
            throw new DomainValidationException("catalog.error.vehicle.notFound");
        }
        vehicleRepository.deleteById(command.vehicleId());
    }
}
