CREATE TABLE p2p_payment_accounts (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    method VARCHAR(50) NOT NULL,
    account_name VARCHAR(120) NOT NULL,
    account_identifier VARCHAR(150) NOT NULL,
    instructions TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_p2p_payment_accounts_owner ON p2p_payment_accounts(owner_id);
CREATE INDEX idx_p2p_payment_accounts_method ON p2p_payment_accounts(owner_id, method, active);
