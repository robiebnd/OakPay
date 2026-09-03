CREATE TABLE p2p_commissions (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL UNIQUE,
    payer_id UUID NOT NULL,
    fiat_currency VARCHAR(10) NOT NULL,
    fiat_amount NUMERIC(38,18) NOT NULL,
    rate NUMERIC(10,8) NOT NULL,
    commission_amount NUMERIC(38,18) NOT NULL,
    status VARCHAR(15) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_p2p_commission_fiat_amount CHECK (fiat_amount > 0),
    CONSTRAINT chk_p2p_commission_rate CHECK (rate >= 0 AND rate <= 1),
    CONSTRAINT chk_p2p_commission_amount CHECK (commission_amount >= 0),
    CONSTRAINT chk_p2p_commission_status CHECK (status IN ('ASSESSED', 'COLLECTED', 'WAIVED'))
);

CREATE INDEX idx_p2p_commission_payer ON p2p_commissions(payer_id);
CREATE INDEX idx_p2p_commission_status ON p2p_commissions(status);
