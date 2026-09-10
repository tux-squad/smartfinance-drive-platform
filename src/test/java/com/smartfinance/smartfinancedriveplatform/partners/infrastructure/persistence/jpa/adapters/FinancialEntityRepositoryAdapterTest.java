package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.FinancialEntityPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories.SpringDataFinancialEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FinancialEntityRepositoryAdapter.
 * Verifies persistence mapping and delegation to the Spring Data JPA layer.
 */
@ExtendWith(MockitoExtension.class)
class FinancialEntityRepositoryAdapterTest {

    @Mock
    private SpringDataFinancialEntityRepository springDataFinancialEntityRepository;

    private FinancialEntityRepositoryAdapter financialEntityRepositoryAdapter;

    @BeforeEach
    void setUp() {
        financialEntityRepositoryAdapter = new FinancialEntityRepositoryAdapter(springDataFinancialEntityRepository);
    }

    @Test
    void testFindByIdFound() {
        UUID id = UUID.randomUUID();
        FinancialEntityPersistenceEntity entity = new FinancialEntityPersistenceEntity();
        entity.setId(id);
        entity.setName("BBVA");

        when(springDataFinancialEntityRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<FinancialEntity> result = financialEntityRepositoryAdapter.findById(new FinancialEntityId(id));

        assertTrue(result.isPresent());
        assertEquals("BBVA", result.get().getName());
    }

    @Test
    void testSaveFinancialEntity() {
        UUID id = UUID.randomUUID();
        FinancialEntity domainEntity = new FinancialEntity(new FinancialEntityId(id), "BBVA", null);

        FinancialEntityPersistenceEntity entity = new FinancialEntityPersistenceEntity();
        entity.setId(id);
        entity.setName("BBVA");

        when(springDataFinancialEntityRepository.findById(id)).thenReturn(Optional.empty());
        when(springDataFinancialEntityRepository.save(any())).thenReturn(entity);

        FinancialEntity result = financialEntityRepositoryAdapter.save(domainEntity);

        assertNotNull(result);
        assertEquals("BBVA", result.getName());
        verify(springDataFinancialEntityRepository, times(1)).save(any());
    }
}
