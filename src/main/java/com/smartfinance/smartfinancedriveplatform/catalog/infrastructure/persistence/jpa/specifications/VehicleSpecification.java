package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.specifications;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifications builder for dynamic filtering of VehiclePersistenceEntity.
 */
public class VehicleSpecification {

    public static Specification<VehiclePersistenceEntity> withFilter(GetAllVehiclesQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query == null) {
                return cb.conjunction();
            }

            if (query.brand() != null && !query.brand().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("brand")), "%" + query.brand().trim().toLowerCase() + "%"));
            }

            if (query.model() != null && !query.model().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("model")), "%" + query.model().trim().toLowerCase() + "%"));
            }

            if (query.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), query.minPrice()));
            }

            if (query.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), query.maxPrice()));
            }

            if (query.minYear() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("manufactureYear"), query.minYear()));
            }

            if (query.maxYear() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("manufactureYear"), query.maxYear()));
            }

            if (query.condition() != null && !query.condition().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("condition")), query.condition().trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
