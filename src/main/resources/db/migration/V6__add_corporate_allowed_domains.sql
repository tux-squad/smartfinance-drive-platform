-- Migration Script to add corporate allowed domains for Financial Entities and Dealerships

-- 1. Allow dealerships to exist unlinked before user claims them
ALTER TABLE dealerships ALTER COLUMN user_id DROP NOT NULL;

-- 2. Create financial_entity_allowed_domains table
CREATE TABLE IF NOT EXISTS financial_entity_allowed_domains (
    financial_entity_id UUID NOT NULL REFERENCES financial_entities(id) ON DELETE CASCADE,
    domain VARCHAR(255) NOT NULL,
    CONSTRAINT uq_fe_domain UNIQUE (financial_entity_id, domain)
);

CREATE INDEX IF NOT EXISTS idx_fe_allowed_domains_entity ON financial_entity_allowed_domains(financial_entity_id);

-- 3. Create dealership_allowed_domains table
CREATE TABLE IF NOT EXISTS dealership_allowed_domains (
    dealership_id UUID NOT NULL REFERENCES dealerships(id) ON DELETE CASCADE,
    domain VARCHAR(255) NOT NULL,
    CONSTRAINT uq_dealership_domain UNIQUE (dealership_id, domain)
);

CREATE INDEX IF NOT EXISTS idx_dealership_allowed_domains_dealership ON dealership_allowed_domains(dealership_id);
