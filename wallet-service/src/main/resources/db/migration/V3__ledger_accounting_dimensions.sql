ALTER TABLE ledger_entries
    ADD COLUMN direction VARCHAR(10) NOT NULL DEFAULT 'CREDIT',
    ADD COLUMN balance_type VARCHAR(10) NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE ledger_entries
    ADD CONSTRAINT chk_ledger_direction CHECK (direction IN ('CREDIT', 'DEBIT')),
    ADD CONSTRAINT chk_ledger_balance_type CHECK (balance_type IN ('AVAILABLE', 'LOCKED'));

CREATE INDEX idx_ledger_balance_type ON ledger_entries(balance_type);
CREATE INDEX idx_ledger_direction ON ledger_entries(direction);
