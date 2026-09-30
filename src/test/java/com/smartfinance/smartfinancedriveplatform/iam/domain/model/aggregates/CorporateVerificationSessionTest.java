package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationSessionStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorporateVerificationSession Aggregate Unit Tests")
class CorporateVerificationSessionTest {

    @Test
    @DisplayName("Should create active session in PENDING state")
    void shouldCreateActiveSessionInPendingState() {
        var session = new CorporateVerificationSession(
                "user-123",
                "20100047218",
                "funcionario@viabcp.com",
                "hash123",
                "FINANCIAL_INSTITUTION",
                "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO DEL PERU",
                "CAL. CENTENARIO 156",
                Instant.now().plus(10, ChronoUnit.MINUTES)
        );

        assertNotNull(session.getId());
        assertEquals("user-123", session.getUserId());
        assertEquals("20100047218", session.getRuc());
        assertEquals("funcionario@viabcp.com", session.getCorporateEmail());
        assertEquals(0, session.getAttempts());
        assertEquals(CorporateVerificationSessionStatus.PENDING, session.getStatus());
        assertTrue(session.isPending());
        assertFalse(session.isBlocked());
        assertFalse(session.isExpired());
    }

    @Test
    @DisplayName("Should block session after 3 failed attempts")
    void shouldBlockSessionAfterThreeFailedAttempts() {
        var session = new CorporateVerificationSession(
                "user-123",
                "20100047218",
                "funcionario@viabcp.com",
                "hash123",
                "FINANCIAL_INSTITUTION",
                "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO DEL PERU",
                "CAL. CENTENARIO 156",
                Instant.now().plus(10, ChronoUnit.MINUTES)
        );

        session.recordFailedAttempt();
        assertEquals(1, session.getAttempts());
        assertTrue(session.isPending());

        session.recordFailedAttempt();
        assertEquals(2, session.getAttempts());
        assertTrue(session.isPending());

        session.recordFailedAttempt();
        assertEquals(3, session.getAttempts());
        assertTrue(session.isBlocked());
        assertEquals(CorporateVerificationSessionStatus.BLOCKED, session.getStatus());
        assertFalse(session.isPending());
    }

    @Test
    @DisplayName("Should recognize expired session")
    void shouldRecognizeExpiredSession() {
        var session = new CorporateVerificationSession(
                "user-123",
                "20100047218",
                "funcionario@viabcp.com",
                "hash123",
                "FINANCIAL_INSTITUTION",
                "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO DEL PERU",
                "CAL. CENTENARIO 156",
                Instant.now().minus(1, ChronoUnit.MINUTES)
        );

        assertTrue(session.isExpired());
        assertFalse(session.isPending());
        assertEquals(CorporateVerificationSessionStatus.EXPIRED, session.getStatus());
    }

    @Test
    @DisplayName("Should successfully mark session as VERIFIED")
    void shouldSuccessfullyMarkSessionAsVerified() {
        var session = new CorporateVerificationSession(
                "user-123",
                "20100047218",
                "funcionario@viabcp.com",
                "hash123",
                "FINANCIAL_INSTITUTION",
                "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO DEL PERU",
                "CAL. CENTENARIO 156",
                Instant.now().plus(10, ChronoUnit.MINUTES)
        );

        session.markVerified();
        assertEquals(CorporateVerificationSessionStatus.VERIFIED, session.getStatus());
        assertFalse(session.isPending());
    }

    @Test
    @DisplayName("Should reject markVerified if session is blocked")
    void shouldRejectMarkVerifiedIfBlocked() {
        var session = new CorporateVerificationSession(
                "user-123",
                "20100047218",
                "funcionario@viabcp.com",
                "hash123",
                "FINANCIAL_INSTITUTION",
                "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO DEL PERU",
                "CAL. CENTENARIO 156",
                Instant.now().plus(10, ChronoUnit.MINUTES)
        );

        session.recordFailedAttempt();
        session.recordFailedAttempt();
        session.recordFailedAttempt();

        assertThrows(DomainValidationException.class, session::markVerified);
    }
}
