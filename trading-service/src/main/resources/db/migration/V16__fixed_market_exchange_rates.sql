CREATE TABLE p2p_exchange_rates (
    id UUID PRIMARY KEY,
    base_currency VARCHAR(10) NOT NULL,
    quote_currency VARCHAR(10) NOT NULL,
    rate NUMERIC(20,2) NOT NULL,
    source VARCHAR(50) NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_p2p_exchange_pair_effective UNIQUE (base_currency, quote_currency, effective_at),
    CONSTRAINT chk_p2p_exchange_rate_positive CHECK (rate > 0),
    CONSTRAINT chk_p2p_exchange_pair_different CHECK (base_currency <> quote_currency)
);

CREATE INDEX idx_p2p_exchange_active_pair
    ON p2p_exchange_rates(base_currency, quote_currency, active, effective_at DESC);

INSERT INTO p2p_exchange_rates (
    id, base_currency, quote_currency, rate, source, effective_at, active, created_at
) VALUES
    ('00000000-0000-0000-0000-000000000801', 'USDT', 'USD', 1.00, 'MARKET_REFERENCE', '2026-09-04 00:00:00', TRUE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000802', 'USDT', 'ZWG', 26.56, 'RBZ_INTERBANK_AVG', '2026-09-04 00:00:00', TRUE, CURRENT_TIMESTAMP);
