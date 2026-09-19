package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.CreditApplicationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.CreditApplicationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetCreditApplicationsByApplicantQuery;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CreditApplicationsController Unit Tests")
class CreditApplicationsControllerTest {

    private CreditApplicationCommandService commandService;
    private CreditApplicationQueryService queryService;
    private CreditApplicationsController controller;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        commandService = mock(CreditApplicationCommandService.class);
        queryService = mock(CreditApplicationQueryService.class);
        controller = new CreditApplicationsController(commandService, queryService);

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getRequiredCurrentUserId).thenReturn("buyer-123");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("Should return user credit applications")
    void shouldReturnUserCreditApplications() {
        CreditApplication application = new CreditApplication(
                "buyer-123",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Money(new BigDecimal("25000.00"), "USD"),
                new Money(new BigDecimal("5000.00"), "USD"),
                36,
                new Money(new BigDecimal("4500.00"), "USD"),
                "EMPLOYED"
        );
        when(queryService.handle(any(GetCreditApplicationsByApplicantQuery.class))).thenReturn(List.of(application));

        var response = controller.getMyCreditApplications();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}
