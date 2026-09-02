CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    side VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    base_currency VARCHAR(10) NOT NULL,
    quote_currency VARCHAR(10) NOT NULL,
    price NUMERIC(38, 18) NOT NULL,
    quantity NUMERIC(38, 18) NOT NULL,
    remaining_quantity NUMERIC(38, 18) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_order_price CHECK (price > 0),
    CONSTRAINT chk_order_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_remaining CHECK (remaining_quantity >= 0 AND remaining_quantity <= quantity),
    CONSTRAINT chk_order_currencies CHECK (base_currency <> quote_currency)
);

CREATE INDEX idx_orders_pair_status ON orders(base_currency, quote_currency, status);
CREATE INDEX idx_orders_user_id ON orders(user_id);

CREATE TABLE trades (
    id UUID PRIMARY KEY,
    buy_order_id UUID NOT NULL,
    sell_order_id UUID NOT NULL,
    buyer_id UUID NOT NULL,
    seller_id UUID NOT NULL,
    base_currency VARCHAR(10) NOT NULL,
    quote_currency VARCHAR(10) NOT NULL,
    price NUMERIC(38, 18) NOT NULL,
    quantity NUMERIC(38, 18) NOT NULL,
    gross_value NUMERIC(38, 18) NOT NULL,
    buyer_fee NUMERIC(38, 18) NOT NULL DEFAULT 0,
    seller_fee NUMERIC(38, 18) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_trade_price CHECK (price > 0),
    CONSTRAINT chk_trade_quantity CHECK (quantity > 0),
    CONSTRAINT chk_trade_gross CHECK (gross_value > 0)
);

CREATE INDEX idx_trades_buyer ON trades(buyer_id);
CREATE INDEX idx_trades_seller ON trades(seller_id);
CREATE INDEX idx_trades_orders ON trades(buy_order_id, sell_order_id);
