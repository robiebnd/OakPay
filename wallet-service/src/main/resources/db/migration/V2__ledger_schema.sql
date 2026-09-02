CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    user_id UUID NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount NUMERIC(38, 18) NOT NULL,
    balance_before NUMERIC(38, 18) NOT NULL,
    balance_after NUMERIC(38, 18) NOT NULL,
    reference VARCHAR(100) NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_ledger_reference UNIQUE (reference),
    CONSTRAINT chk_ledger_amount CHECK (amount > 0),
    CONSTRAINT chk_ledger_balance_before CHECK (balance_before >= 0),
    CONSTRAINT chk_ledger_balance_after CHECK (balance_after >= 0)
);

CREATE INDEX idx_ledger_user_created ON ledger_entries(user_id, created_at DESC);
CREATE INDEX idx_ledger_wallet_created ON ledger_entries(wallet_id, created_at DESC);
CREATE INDEX idx_ledger_currency ON ledger_entries(currency);
