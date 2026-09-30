package com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security;

import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OwnershipChecker Unit Tests")
class OwnershipCheckerTest {

    @Mock
    private FinancialEntityQueryService financialEntityQueryService;

    @InjectMocks
    private OwnershipChecker ownershipChecker;

    private final UUID entityId = UUID.randomUUID();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, "credentials", List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails(userId, userId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("isFinancialEntityOwner returns true when authenticated user is the entity owner")
    void returnsTrueWhenUserOwnsFinancialEntity() {
        authenticateAs("bank-user-1", "FINANCIAL_INSTITUTION");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                "bank-user-1",
                "Banco BCP",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityQueryService.handle(any(GetFinancialEntityByIdQuery.class)))
                .thenReturn(Optional.of(entity));

        boolean isOwner = ownershipChecker.isFinancialEntityOwner(entityId, SecurityContextHolder.getContext().getAuthentication());
        assertTrue(isOwner);
    }

    @Test
    @DisplayName("isFinancialEntityOwner returns false when authenticated user does NOT own the entity")
    void returnsFalseWhenUserDoesNotOwnFinancialEntity() {
        authenticateAs("bank-user-attacker", "FINANCIAL_INSTITUTION");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                "bank-user-1",
                "Banco BCP",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityQueryService.handle(any(GetFinancialEntityByIdQuery.class)))
                .thenReturn(Optional.of(entity));

        boolean isOwner = ownershipChecker.isFinancialEntityOwner(entityId, SecurityContextHolder.getContext().getAuthentication());
        assertFalse(isOwner);
    }

    @Test
    @DisplayName("isFinancialEntityOwner returns true for ADMIN regardless of entity owner")
    void returnsTrueForAdmin() {
        authenticateAs("admin-user", "ADMIN");

        boolean isOwner = ownershipChecker.isFinancialEntityOwner(entityId, SecurityContextHolder.getContext().getAuthentication());
        assertTrue(isOwner);
    }

    @Test
    @DisplayName("isUserSelfStr returns true when requesting own user ID")
    void isUserSelfStrReturnsTrueForSameUser() {
        authenticateAs("dealer-123", "DEALER");

        boolean isSelf = ownershipChecker.isUserSelfStr("dealer-123", SecurityContextHolder.getContext().getAuthentication());
        assertTrue(isSelf);
    }

    @Test
    @DisplayName("isUserSelfStr returns false when requesting different user ID")
    void isUserSelfStrReturnsFalseForDifferentUser() {
        authenticateAs("dealer-123", "DEALER");

        boolean isSelf = ownershipChecker.isUserSelfStr("dealer-other", SecurityContextHolder.getContext().getAuthentication());
        assertFalse(isSelf);
    }

    @Test
    @DisplayName("isUserSelfStr returns true for ADMIN regardless of user ID")
    void isUserSelfStrReturnsTrueForAdmin() {
        authenticateAs("admin-user", "ADMIN");

        boolean isSelf = ownershipChecker.isUserSelfStr("dealer-other", SecurityContextHolder.getContext().getAuthentication());
        assertTrue(isSelf);
    }
}
