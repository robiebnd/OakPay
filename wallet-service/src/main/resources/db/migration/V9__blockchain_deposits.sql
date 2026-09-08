CREATE TABLE deposits (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    deposit_address_id UUID NOT NULL,
    currency VARCHAR(10) NOT NULL,
    network VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tx_hash VARCHAR(150) NOT NULL,
    amount NUMERIC(30,8) NOT NULL,
    confirmations INTEGER NOT NULL DEFAULT 0,
    required_confirmations INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason TEXT,
    detected_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_deposit_tx_network UNIQUE (tx_hash, network),
    CONSTRAINT ck_deposit_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_deposit_confirmations_nonnegative CHECK (confirmations >= 0),
    CONSTRAINT ck_deposit_required_confirmations_positive CHECK (required_confirmations > 0),
    CONSTRAINT ck_deposit_status CHECK (status IN ('PENDING', 'CONFIRMING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_deposits_user_detected
    ON deposits (user_id, detected_at DESC);

CREATE INDEX idx_deposits_address_status
    ON deposits (deposit_address_id, status);

CREATE INDEX idx_deposits_tx_hash
    ON deposits (tx_hash);
