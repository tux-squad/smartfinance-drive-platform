package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.SimulationRepository;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.assemblers.SimulationPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.SimulationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories.SpringDataSimulationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing the SimulationRepository interface from the domain layer
 * by delegating to Spring Data JPA and assembling results.
 */
@Component
public class SimulationRepositoryAdapter implements SimulationRepository {

    private final SpringDataSimulationRepository springDataSimulationRepository;

    public SimulationRepositoryAdapter(SpringDataSimulationRepository springDataSimulationRepository) {
        this.springDataSimulationRepository = springDataSimulationRepository;
    }

    @Override
    public Simulation save(Simulation simulation) {
        SimulationPersistenceEntity existingEntity = springDataSimulationRepository.findById(simulation.getId().value()).orElse(null);
        SimulationPersistenceEntity entityToSave = SimulationPersistenceAssembler.toEntity(simulation, existingEntity);
        SimulationPersistenceEntity savedEntity = springDataSimulationRepository.save(entityToSave);
        return SimulationPersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<Simulation> findById(SimulationId id) {
        return springDataSimulationRepository.findById(id.value())
                .map(SimulationPersistenceAssembler::toDomain);
    }

    @Override
    public List<Simulation> findAll() {
        return springDataSimulationRepository.findAll().stream()
                .map(SimulationPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(SimulationId id) {
        springDataSimulationRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(SimulationId id) {
        return springDataSimulationRepository.existsById(id.value());
    }
}
