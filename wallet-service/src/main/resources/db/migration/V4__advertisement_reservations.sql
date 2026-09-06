CREATE TABLE advertisement_reservations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    currency VARCHAR(20) NOT NULL,
    reference VARCHAR(100) NOT NULL UNIQUE,
    reserved_amount NUMERIC(38,18) NOT NULL,
    status VARCHAR(15) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_ad_res_amount CHECK (reserved_amount >= 0),
    CONSTRAINT chk_ad_res_status CHECK (status IN ('RESERVED','CONSUMED','RELEASED'))
);

CREATE INDEX idx_ad_res_user_currency ON advertisement_reservations(user_id, currency);
CREATE INDEX idx_ad_res_status ON advertisement_reservations(status);
