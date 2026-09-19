CREATE TABLE custody_webhook_events (
    id UUID PRIMARY KEY,
    provider_name VARCHAR(80) NOT NULL,
    event_id VARCHAR(150) NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deposit_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_custody_webhook_provider_event UNIQUE (provider_name, event_id),
    CONSTRAINT ck_custody_webhook_event_status CHECK (status IN ('PROCESSING', 'PROCESSED'))
);

CREATE INDEX idx_custody_webhook_events_deposit
    ON custody_webhook_events(deposit_id);

CREATE INDEX idx_custody_webhook_events_created
    ON custody_webhook_events(created_at DESC);
