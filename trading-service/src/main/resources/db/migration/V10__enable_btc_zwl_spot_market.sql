INSERT INTO supported_assets
(id, symbol, name, asset_type, min_trade_amount, min_deposit_amount, min_withdrawal_amount,
 decimal_places, status, p2p_enabled, spot_enabled, created_at, updated_at)
VALUES
(gen_random_uuid(), 'ZWL', 'Zimbabwean Dollar', 'FIAT', 1.00, 1.00, 1.00, 2, 'ACTIVE', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (symbol) DO UPDATE
SET status = 'ACTIVE',
    spot_enabled = TRUE,
    updated_at = NOW();

UPDATE supported_assets
SET spot_enabled = TRUE,
    status = 'ACTIVE',
    updated_at = NOW()
WHERE symbol = 'BTC';
