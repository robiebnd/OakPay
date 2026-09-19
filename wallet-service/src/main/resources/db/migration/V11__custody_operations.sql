CREATE TABLE custody_operations (
    id UUID PRIMARY KEY,
    operation_type VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL,
    provider_name VARCHAR(80) NOT NULL,
    provider_reference VARCHAR(150),
    user_id UUID NOT NULL,
    currency VARCHAR(10) NOT NULL,
    network VARCHAR(30),
    amount NUMERIC(30,8),
    destination_address VARCHAR(255),
    memo_tag VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_custody_operation_type_key UNIQUE (operation_type, idempotency_key),
    CONSTRAINT uk_custody_operation_provider_ref UNIQUE (provider_name, provider_reference),
    CONSTRAINT ck_custody_operation_type CHECK (operation_type IN ('DEPOSIT_ADDRESS', 'WITHDRAWAL')),
    CONSTRAINT ck_custody_operation_status CHECK (status IN ('REQUESTED', 'SUBMITTED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_custody_operation_amount_positive CHECK (amount IS NULL OR amount > 0)
);

CREATE INDEX idx_custody_operations_user_created
    ON custody_operations(user_id, created_at DESC);

CREATE INDEX idx_custody_operations_provider_ref
    ON custody_operations(provider_name, provider_reference);
