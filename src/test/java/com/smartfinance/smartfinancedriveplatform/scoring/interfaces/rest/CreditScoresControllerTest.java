package com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.scoring.application.commandservices.CreditScoreCommandService;
import com.smartfinance.smartfinancedriveplatform.scoring.application.queryservices.CreditScoreQueryService;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.DeleteCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.EvaluateCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetAllCreditScoresQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByProfileIdQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources.CreditScoreResource;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources.EvaluateCreditScoreResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditScoresController REST Unit Tests")
class CreditScoresControllerTest {

    @Mock
    private CreditScoreCommandService creditScoreCommandService;

    @Mock
    private CreditScoreQueryService creditScoreQueryService;

    @Mock
    private OwnershipChecker ownershipChecker;

    @InjectMocks
    private CreditScoresController creditScoresController;

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
    @DisplayName("Should evaluate credit score and return 201 Created")
    void shouldEvaluateCreditScore() {
        EvaluateCreditScoreResource resource = new EvaluateCreditScoreResource(
                "prof-100",
                "sim-200",
                new BigDecimal("5000.00"),
                new BigDecimal("1200.00"),
                "PEN"
        );

        when(creditScoreCommandService.handle(any(EvaluateCreditScoreCommand.class))).thenReturn(Optional.of(sampleScore));

        ResponseEntity<CreditScoreResource> response = creditScoresController.evaluateCreditScore(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("prof-100", response.getBody().profileId());
        assertEquals("TIER_A", response.getBody().riskTier());
    }

    @Test
    @DisplayName("Should get all credit scores and return 200 OK")
    void shouldGetAllCreditScores() {
        when(creditScoreQueryService.handle(any(GetAllCreditScoresQuery.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(sampleScore)));
        when(ownershipChecker.isCreditScoreOwner(any(), any())).thenReturn(true);

        ResponseEntity<org.springframework.data.domain.Page<CreditScoreResource>> response =
                creditScoresController.getAllCreditScores(org.springframework.data.domain.Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    @DisplayName("Should get credit scores by profile ID and return 200 OK")
    void shouldGetCreditScoresByProfileId() {
        when(creditScoreQueryService.handle(any(GetCreditScoreByProfileIdQuery.class))).thenReturn(List.of(sampleScore));

        ResponseEntity<List<CreditScoreResource>> response = creditScoresController.getCreditScoresByProfileId("prof-100");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should delete credit score and return 204 No Content")
    void shouldDeleteCreditScore() {
        UUID scoreId = UUID.randomUUID();
        doNothing().when(creditScoreCommandService).handle(any(DeleteCreditScoreCommand.class));

        ResponseEntity<?> response = creditScoresController.deleteCreditScore(scoreId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(creditScoreCommandService, times(1)).handle(any(DeleteCreditScoreCommand.class));
    }
}
