CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    currency VARCHAR(10) NOT NULL,
    available_balance NUMERIC(38, 18) NOT NULL DEFAULT 0,
    locked_balance NUMERIC(38, 18) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_wallet_user_currency UNIQUE (user_id, currency),
    CONSTRAINT chk_wallet_available_balance CHECK (available_balance >= 0),
    CONSTRAINT chk_wallet_locked_balance CHECK (locked_balance >= 0)
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_currency ON wallets(currency);
