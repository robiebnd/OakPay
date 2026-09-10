CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_email_verification_user ON email_verification_tokens (user_id);
CREATE INDEX idx_email_verification_expires ON email_verification_tokens (expires_at);
