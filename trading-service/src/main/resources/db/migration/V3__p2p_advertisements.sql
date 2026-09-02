CREATE TABLE p2p_advertisements (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    side VARCHAR(10) NOT NULL,
    asset VARCHAR(10) NOT NULL,
    fiat_currency VARCHAR(10) NOT NULL,
    price NUMERIC(38, 18) NOT NULL,
    total_quantity NUMERIC(38, 18) NOT NULL,
    available_quantity NUMERIC(38, 18) NOT NULL,
    min_quantity NUMERIC(38, 18) NOT NULL,
    max_quantity NUMERIC(38, 18) NOT NULL,
    payment_methods VARCHAR(500) NOT NULL,
    terms TEXT,
    status VARCHAR(15) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_ad_price CHECK (price > 0),
    CONSTRAINT chk_ad_total_quantity CHECK (total_quantity > 0),
    CONSTRAINT chk_ad_available_quantity CHECK (available_quantity >= 0 AND available_quantity <= total_quantity),
    CONSTRAINT chk_ad_min_quantity CHECK (min_quantity > 0),
    CONSTRAINT chk_ad_max_quantity CHECK (max_quantity >= min_quantity AND max_quantity <= total_quantity),
    CONSTRAINT chk_ad_currencies CHECK (asset <> fiat_currency)
);

CREATE INDEX idx_p2p_ads_search ON p2p_advertisements(side, asset, fiat_currency, status);
CREATE INDEX idx_p2p_ads_owner ON p2p_advertisements(owner_id);
