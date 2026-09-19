ALTER TABLE custody_webhook_events
    ADD COLUMN withdrawal_operation_id UUID;

CREATE INDEX idx_custody_webhook_events_withdrawal
    ON custody_webhook_events(withdrawal_operation_id);
