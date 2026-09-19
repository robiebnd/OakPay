CREATE TABLE platform_fee_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value NUMERIC(20,10) NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP NOT NULL
);
