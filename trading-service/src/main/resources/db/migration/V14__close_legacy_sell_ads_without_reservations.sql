UPDATE p2p_advertisements
SET status = 'CLOSED',
    auto_closed = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE side = 'SELL'
  AND reservation_reference IS NULL
  AND status IN ('ACTIVE', 'PAUSED');
