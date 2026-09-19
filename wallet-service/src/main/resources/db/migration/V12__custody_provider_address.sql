ALTER TABLE custody_operations
    ADD COLUMN provider_address VARCHAR(255);

CREATE INDEX idx_custody_operations_provider_address
    ON custody_operations(provider_name, provider_address);
