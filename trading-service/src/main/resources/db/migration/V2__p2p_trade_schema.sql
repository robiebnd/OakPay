CREATE TABLE p2p_trades (
    id UUID PRIMARY KEY,
    buyer_id UUID NOT NULL,
    seller_id UUID NOT NULL,
    asset VARCHAR(10) NOT NULL,
    fiat_currency VARCHAR(10) NOT NULL,
    quantity NUMERIC(38,18) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(38,18) NOT NULL CHECK (unit_price > 0),
    fiat_amount NUMERIC(38,18) NOT NULL CHECK (fiat_amount > 0),
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(25) NOT NULL,
    payment_reference VARCHAR(150),
    payment_note TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_p2p_users_differ CHECK (buyer_id <> seller_id)
);

CREATE INDEX idx_p2p_buyer ON p2p_trades(buyer_id);
CREATE INDEX idx_p2p_seller ON p2p_trades(seller_id);
CREATE INDEX idx_p2p_status ON p2p_trades(status);
