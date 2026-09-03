ALTER TABLE p2p_commissions
    ADD COLUMN collection_reference VARCHAR(100),
    ADD COLUMN collection_method VARCHAR(30),
    ADD COLUMN collected_at TIMESTAMP;

CREATE INDEX idx_p2p_commission_collection_reference
    ON p2p_commissions(collection_reference);
