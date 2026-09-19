CREATE TABLE wallet_operations (
    id UUID PRIMARY KEY,
    operation_type VARCHAR(30) NOT NULL,
    reference VARCHAR(100) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount NUMERIC(30,8) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_wallet_operation_type_reference UNIQUE (operation_type, reference)
);

CREATE INDEX idx_wallet_operations_user_created
    ON wallet_operations(user_id, created_at DESC);
