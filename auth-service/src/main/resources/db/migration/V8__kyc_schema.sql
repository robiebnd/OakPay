CREATE TABLE kyc_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    rejection_reason VARCHAR(500),
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_kyc_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE identity_documents (
    id UUID PRIMARY KEY,
    kyc_profile_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    document_number VARCHAR(100),
    front_document_path VARCHAR(500),
    back_document_path VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_identity_documents_kyc FOREIGN KEY (kyc_profile_id) REFERENCES kyc_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_identity_documents_kyc_profile ON identity_documents(kyc_profile_id);
