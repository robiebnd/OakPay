-- Phase 6: idempotency protection for externally retried financial commands.
ALTER TABLE p2p_trades
    ADD COLUMN idempotency_key VARCHAR(100),
    ADD COLUMN idempotency_hash VARCHAR(64);

CREATE UNIQUE INDEX uk_p2p_trade_idempotency_key
    ON p2p_trades(idempotency_key)
    WHERE idempotency_key IS NOT NULL;

ALTER TABLE orders
    ADD COLUMN idempotency_key VARCHAR(100),
    ADD COLUMN idempotency_hash VARCHAR(64);

CREATE UNIQUE INDEX uk_order_idempotency_key
    ON orders(idempotency_key)
    WHERE idempotency_key IS NOT NULL;
