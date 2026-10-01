-- Migration Script to create phone_verification_sessions table for WhatsApp OTP validation

CREATE TABLE IF NOT EXISTS phone_verification_sessions (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255),
    phone_number VARCHAR(20) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verification_token VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_phone_verif_number ON phone_verification_sessions(phone_number);
CREATE INDEX IF NOT EXISTS idx_phone_verif_status ON phone_verification_sessions(status);
CREATE INDEX IF NOT EXISTS idx_phone_verif_user_id ON phone_verification_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_phone_verif_token ON phone_verification_sessions(verification_token);
