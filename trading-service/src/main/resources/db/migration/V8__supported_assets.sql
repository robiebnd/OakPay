CREATE TABLE supported_assets (
    id UUID PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(20) NOT NULL,
    min_trade_amount NUMERIC(38,18) NOT NULL,
    min_deposit_amount NUMERIC(38,18) NOT NULL,
    min_withdrawal_amount NUMERIC(38,18) NOT NULL,
    decimal_places INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    p2p_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    spot_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_supported_asset_limits CHECK (
        min_trade_amount > 0 AND min_deposit_amount > 0 AND min_withdrawal_amount > 0
    ),
    CONSTRAINT chk_supported_asset_decimals CHECK (decimal_places BETWEEN 0 AND 18),
    CONSTRAINT chk_supported_asset_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'DISABLED'))
);

CREATE INDEX idx_supported_asset_status ON supported_assets(status);

INSERT INTO supported_assets
(id, symbol, name, asset_type, min_trade_amount, min_deposit_amount, min_withdrawal_amount,
 decimal_places, status, p2p_enabled, spot_enabled, created_at, updated_at)
VALUES
(gen_random_uuid(), 'BTC', 'Bitcoin', 'CRYPTO', 0.00010000, 0.00010000, 0.00020000, 8, 'ACTIVE', TRUE, FALSE, NOW(), NOW()),
(gen_random_uuid(), 'USDT', 'Tether USD', 'STABLECOIN', 1.00000000, 1.00000000, 5.00000000, 6, 'ACTIVE', TRUE, FALSE, NOW(), NOW()),
(gen_random_uuid(), 'USDC', 'USD Coin', 'STABLECOIN', 1.00000000, 1.00000000, 5.00000000, 6, 'ACTIVE', TRUE, FALSE, NOW(), NOW());
