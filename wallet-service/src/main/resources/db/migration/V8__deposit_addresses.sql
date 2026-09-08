CREATE TABLE deposit_addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    currency VARCHAR(10) NOT NULL,
    network VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    memo_tag VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_deposit_address_user_asset_network UNIQUE (user_id, currency, network),
    CONSTRAINT uk_deposit_address_address_network UNIQUE (address, network)
);

CREATE INDEX idx_deposit_addresses_user_status
    ON deposit_addresses (user_id, status);
