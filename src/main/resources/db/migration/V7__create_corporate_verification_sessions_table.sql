-- Migration Script to create corporate_verification_sessions table for B2B OTP validation

CREATE TABLE IF NOT EXISTS corporate_verification_sessions (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    ruc VARCHAR(11) NOT NULL,
    corporate_email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    target_role VARCHAR(50) NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    fiscal_address VARCHAR(500),
    attempts INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_corp_verif_user_ruc ON corporate_verification_sessions(user_id, ruc);
CREATE INDEX IF NOT EXISTS idx_corp_verif_status ON corporate_verification_sessions(status);
