CREATE TABLE pending_registrations (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    verification_code_hash VARCHAR(64) NOT NULL,
    verification_expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_pending_registrations_email UNIQUE (email),
    CONSTRAINT uk_pending_registrations_code_hash UNIQUE (verification_code_hash)
);

CREATE INDEX idx_pending_registrations_email ON pending_registrations (email);
