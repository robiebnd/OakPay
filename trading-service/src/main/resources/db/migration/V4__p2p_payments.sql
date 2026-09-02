CREATE TABLE p2p_payments (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL UNIQUE,
    payer_id UUID NOT NULL,
    payee_id UUID NOT NULL,
    amount NUMERIC(38, 18) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_reference VARCHAR(150) NOT NULL,
    note TEXT,
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_p2p_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_p2p_payment_users_differ CHECK (payer_id <> payee_id)
);

CREATE INDEX idx_p2p_payments_trade ON p2p_payments(trade_id);
CREATE INDEX idx_p2p_payments_payer ON p2p_payments(payer_id);
CREATE INDEX idx_p2p_payments_status ON p2p_payments(status);
