package com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class enabling Spring Data JPA Auditing.
 * Automatically populates @CreatedDate and @LastModifiedDate fields
 * in entities extending AuditableAbstractPersistenceEntity.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
