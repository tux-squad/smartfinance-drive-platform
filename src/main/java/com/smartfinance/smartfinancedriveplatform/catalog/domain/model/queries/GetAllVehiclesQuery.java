package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries;

import java.math.BigDecimal;

/**
 * Query to find all vehicles matching search/filter criteria.
 *
 * @param brand       Brand substring filter (case-insensitive).
 * @param model       Model substring filter (case-insensitive).
 * @param minPrice    Minimum price filter.
 * @param maxPrice    Maximum price filter.
 * @param minYear     Minimum manufacture year filter.
 * @param maxYear     Maximum manufacture year filter.
 * @param condition   Vehicle condition filter ("NEW" or "USED").
 */
public record GetAllVehiclesQuery(
        String brand,
        String model,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minYear,
        Integer maxYear,
        String condition
) {
    public GetAllVehiclesQuery() {
        this(null, null, null, null, null, null, null);
    }

    public boolean hasAnyFilter() {
        return (brand != null && !brand.isBlank())
                || (model != null && !model.isBlank())
                || minPrice != null || maxPrice != null
                || minYear != null || maxYear != null
                || (condition != null && !condition.isBlank());
    }
}
