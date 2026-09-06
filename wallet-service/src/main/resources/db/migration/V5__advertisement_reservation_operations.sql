CREATE TABLE advertisement_reservation_operations (
    id UUID PRIMARY KEY,
    reservation_reference VARCHAR(100) NOT NULL,
    operation_reference VARCHAR(100) NOT NULL,
    amount NUMERIC(38,18) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_ad_res_operation UNIQUE (reservation_reference, operation_reference),
    CONSTRAINT chk_ad_res_operation_amount CHECK (amount > 0)
);
