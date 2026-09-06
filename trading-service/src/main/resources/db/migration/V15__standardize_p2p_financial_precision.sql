ALTER TABLE orders
    ALTER COLUMN price TYPE NUMERIC(20,2) USING ROUND(price, 2),
    ALTER COLUMN quantity TYPE NUMERIC(20,2) USING ROUND(quantity, 2),
    ALTER COLUMN remaining_quantity TYPE NUMERIC(20,2) USING ROUND(remaining_quantity, 2);

ALTER TABLE trades
    ALTER COLUMN price TYPE NUMERIC(20,2) USING ROUND(price, 2),
    ALTER COLUMN quantity TYPE NUMERIC(20,2) USING ROUND(quantity, 2),
    ALTER COLUMN gross_value TYPE NUMERIC(20,2) USING ROUND(gross_value, 2),
    ALTER COLUMN buyer_fee TYPE NUMERIC(20,2) USING ROUND(buyer_fee, 2),
    ALTER COLUMN seller_fee TYPE NUMERIC(20,2) USING ROUND(seller_fee, 2);

ALTER TABLE p2p_trades
    ALTER COLUMN quantity TYPE NUMERIC(20,2) USING ROUND(quantity, 2),
    ALTER COLUMN unit_price TYPE NUMERIC(20,2) USING ROUND(unit_price, 2),
    ALTER COLUMN fiat_amount TYPE NUMERIC(20,2) USING ROUND(fiat_amount, 2);

ALTER TABLE p2p_advertisements
    ALTER COLUMN price TYPE NUMERIC(20,2) USING ROUND(price, 2),
    ALTER COLUMN total_quantity TYPE NUMERIC(20,2) USING ROUND(total_quantity, 2),
    ALTER COLUMN available_quantity TYPE NUMERIC(20,2) USING ROUND(available_quantity, 2),
    ALTER COLUMN min_quantity TYPE NUMERIC(20,2) USING ROUND(min_quantity, 2),
    ALTER COLUMN max_quantity TYPE NUMERIC(20,2) USING ROUND(max_quantity, 2);
