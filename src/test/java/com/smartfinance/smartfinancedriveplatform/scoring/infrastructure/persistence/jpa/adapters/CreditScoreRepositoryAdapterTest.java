package com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.entities.CreditScorePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.repositories.SpringDataCreditScoreRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditScoreRepositoryAdapter Tests")
class CreditScoreRepositoryAdapterTest {

    @Mock
    private SpringDataCreditScoreRepository springDataCreditScoreRepository;

    @InjectMocks
    private CreditScoreRepositoryAdapter repositoryAdapter;

    private CreditScore sampleScore;

    @BeforeEach
    void setUp() {
        sampleScore = new CreditScore(
                "prof-100",
                "sim-200",
                Money.of(5000.0, "PEN"),
                Money.of(1200.0, "PEN")
        );
    }

    @Test
    @DisplayName("Should save credit score successfully via Spring Data JPA")
    void shouldSaveCreditScoreSuccessfully() {
        when(springDataCreditScoreRepository.findById(any())).thenReturn(Optional.empty());
        when(springDataCreditScoreRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreditScore saved = repositoryAdapter.save(sampleScore);

        assertNotNull(saved);
        assertEquals("prof-100", saved.getProfileId());
        verify(springDataCreditScoreRepository, times(1)).save(any(CreditScorePersistenceEntity.class));
    }

    @Test
    @DisplayName("Should find credit scores by profileId")
    void shouldFindCreditScoresByProfileId() {
        when(springDataCreditScoreRepository.findByProfileId("prof-100")).thenReturn(List.of());

        List<CreditScore> results = repositoryAdapter.findByProfileId("prof-100");

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(springDataCreditScoreRepository, times(1)).findByProfileId("prof-100");
    }
}
