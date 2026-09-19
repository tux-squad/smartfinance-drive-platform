package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SalesAgent Aggregate Root Unit Tests")
class SalesAgentTest {

    @Test
    @DisplayName("Should create SalesAgent successfully")
    void shouldCreateSalesAgentSuccessfully() {
        SalesAgent agent = new SalesAgent("dealer-user-1", "Carlos Mendoza", "carlos@dealer.com", "987654321");

        assertThat(agent.getId()).isNotNull();
        assertThat(agent.getDealerUserId()).isEqualTo("dealer-user-1");
        assertThat(agent.getFullName()).isEqualTo("Carlos Mendoza");
        assertThat(agent.getEmail()).isEqualTo("carlos@dealer.com");
        assertThat(agent.getPhone()).isEqualTo("987654321");
        assertThat(agent.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when dealerUserId is blank")
    void shouldThrowExceptionWhenDealerUserIdIsBlank() {
        assertThatThrownBy(() -> new SalesAgent("", "Carlos Mendoza", "carlos@dealer.com", "987654321"))
                .isInstanceOf(DomainValidationException.class);
    }
}
