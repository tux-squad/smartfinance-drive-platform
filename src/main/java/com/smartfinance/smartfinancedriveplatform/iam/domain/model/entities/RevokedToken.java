package com.smartfinance.smartfinancedriveplatform.iam.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a revoked JWT token or token identifier (jti).
 */
@Entity
@Table(name = "revoked_tokens", indexes = {
    @Index(name = "idx_revoked_token_identifier", columnList = "token_identifier")
})
@Getter
@Setter
@NoArgsConstructor
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_identifier", nullable = false, unique = true, length = 512)
    private String tokenIdentifier;

    @Column(name = "expiration_time_millis", nullable = false)
    private Long expirationTimeMillis;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    public RevokedToken(String tokenIdentifier, Long expirationTimeMillis) {
        this.tokenIdentifier = tokenIdentifier;
        this.expirationTimeMillis = expirationTimeMillis;
        this.revokedAt = LocalDateTime.now();
    }
}
