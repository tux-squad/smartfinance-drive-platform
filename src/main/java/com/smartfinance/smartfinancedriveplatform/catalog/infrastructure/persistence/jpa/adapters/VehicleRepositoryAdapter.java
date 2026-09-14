package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.repositories.VehicleRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.assemblers.VehiclePersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.repositories.SpringDataVehicleRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.specifications.VehicleSpecification;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.services.FuzzySearchUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing the VehicleRepository interface from the domain layer
 * by delegating to Spring Data JPA and assembling results.
 */
@Component
public class VehicleRepositoryAdapter implements VehicleRepository {

    private final SpringDataVehicleRepository springDataVehicleRepository;

    public VehicleRepositoryAdapter(SpringDataVehicleRepository springDataVehicleRepository) {
        this.springDataVehicleRepository = springDataVehicleRepository;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehiclePersistenceEntity existingEntity = springDataVehicleRepository.findById(vehicle.getId().value()).orElse(null);
        VehiclePersistenceEntity entityToSave = VehiclePersistenceAssembler.toEntity(vehicle, existingEntity);
        VehiclePersistenceEntity savedEntity = springDataVehicleRepository.save(entityToSave);
        return VehiclePersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<Vehicle> findById(VehicleId id) {
        return springDataVehicleRepository.findById(id.value())
                .map(VehiclePersistenceAssembler::toDomain);
    }

    @Override
    public List<Vehicle> findAllByUserId(UserId userId) {
        return springDataVehicleRepository.findAllByUserId(userId.value()).stream()
                .map(VehiclePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Vehicle> findAll(GetAllVehiclesQuery query, Pageable pageable) {
        // Step 1: Strict search with JPA Specification
        var spec = VehicleSpecification.withFilter(query);
        Page<VehiclePersistenceEntity> primaryPage = springDataVehicleRepository.findAll(spec, pageable);

        if (primaryPage.getTotalElements() > 0) {
            return primaryPage.map(VehiclePersistenceAssembler::toDomain);
        }

        // Step 2: Fuzzy Search (Levenshtein distance + Trigram similarity for typo tolerance)
        boolean hasBrandQuery = query.brand() != null && !query.brand().isBlank();
        boolean hasModelQuery = query.model() != null && !query.model().isBlank();

        if (hasBrandQuery || hasModelQuery) {
            List<VehiclePersistenceEntity> allEntities = springDataVehicleRepository.findAll();
            List<VehiclePersistenceEntity> fuzzyMatches = allEntities.stream()
                    .filter(e -> {
                        boolean brandMatch = !hasBrandQuery || FuzzySearchUtils.isFuzzyMatch(query.brand(), e.getBrand());
                        boolean modelMatch = !hasModelQuery || FuzzySearchUtils.isFuzzyMatch(query.model(), e.getModel());
                        return brandMatch && modelMatch;
                    })
                    .collect(Collectors.toList());

            if (!fuzzyMatches.isEmpty()) {
                return paginateEntitiesList(fuzzyMatches, pageable);
            }
        }

        // Step 3: Fallback Ampliado (Relax restrictive price/year/condition filters if no exact/fuzzy match found)
        if (query.hasAnyFilter()) {
            var relaxedQuery = new GetAllVehiclesQuery(query.brand(), query.model(), null, null, null, null, null);
            var relaxedSpec = VehicleSpecification.withFilter(relaxedQuery);
            Page<VehiclePersistenceEntity> relaxedPage = springDataVehicleRepository.findAll(relaxedSpec, pageable);

            if (relaxedPage.getTotalElements() > 0) {
                return relaxedPage.map(VehiclePersistenceAssembler::toDomain);
            }
        }

        return Page.empty(pageable);
    }

    private Page<Vehicle> paginateEntitiesList(List<VehiclePersistenceEntity> entities, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), entities.size());

        if (start > entities.size()) {
            return Page.empty(pageable);
        }

        List<Vehicle> pageContent = entities.subList(start, end).stream()
                .map(VehiclePersistenceAssembler::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, entities.size());
    }

    @Override
    public boolean existsById(VehicleId id) {
        return springDataVehicleRepository.existsById(id.value());
    }

    @Override
    public void deleteById(VehicleId id) {
        springDataVehicleRepository.deleteById(id.value());
    }
}
