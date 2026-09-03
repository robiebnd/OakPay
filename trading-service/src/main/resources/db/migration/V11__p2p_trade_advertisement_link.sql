ALTER TABLE p2p_trades
    ADD COLUMN advertisement_id UUID;

CREATE INDEX idx_p2p_advertisement ON p2p_trades(advertisement_id);

ALTER TABLE p2p_trades
    ADD CONSTRAINT fk_p2p_trade_advertisement
    FOREIGN KEY (advertisement_id) REFERENCES p2p_advertisements(id);
