-- Migration Script to create email_verification_sessions table for email OTP validation

CREATE TABLE IF NOT EXISTS email_verification_sessions (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verification_token VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_email_verif_email ON email_verification_sessions(email);
CREATE INDEX IF NOT EXISTS idx_email_verif_status ON email_verification_sessions(status);
CREATE INDEX IF NOT EXISTS idx_email_verif_token ON email_verification_sessions(verification_token);
