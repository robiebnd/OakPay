CREATE TABLE trading_pairs (
    id UUID PRIMARY KEY,
    symbol VARCHAR(30) NOT NULL UNIQUE,
    base_currency VARCHAR(20) NOT NULL,
    quote_currency VARCHAR(20) NOT NULL,
    min_price NUMERIC(38,18) NOT NULL,
    max_price NUMERIC(38,18) NOT NULL,
    price_tick_size NUMERIC(38,18) NOT NULL,
    min_quantity NUMERIC(38,18) NOT NULL,
    quantity_step_size NUMERIC(38,18) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_trading_pair_currencies UNIQUE (base_currency, quote_currency),
    CONSTRAINT chk_trading_pair_price_limits CHECK (min_price > 0 AND max_price > 0 AND min_price <= max_price),
    CONSTRAINT chk_trading_pair_price_tick CHECK (price_tick_size > 0),
    CONSTRAINT chk_trading_pair_quantity_limits CHECK (min_quantity > 0 AND quantity_step_size > 0),
    CONSTRAINT chk_trading_pair_different_currencies CHECK (base_currency <> quote_currency)
);

CREATE INDEX idx_trading_pair_status ON trading_pairs(status);
CREATE INDEX idx_trading_pair_currencies ON trading_pairs(base_currency, quote_currency);

INSERT INTO trading_pairs (
    id, symbol, base_currency, quote_currency,
    min_price, max_price, price_tick_size,
    min_quantity, quantity_step_size, status,
    created_at, updated_at
) VALUES (
    gen_random_uuid(), 'BTCZWL', 'BTC', 'ZWL',
    0.01, 1000000000000, 0.01,
    0.000001, 0.000001, 'ACTIVE',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
