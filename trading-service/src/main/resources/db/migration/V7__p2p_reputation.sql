CREATE TABLE p2p_ratings (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL,
    rater_id UUID NOT NULL,
    rated_user_id UUID NOT NULL,
    score INTEGER NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_p2p_rating_trade_rater UNIQUE (trade_id, rater_id),
    CONSTRAINT chk_p2p_rating_score CHECK (score BETWEEN 1 AND 5)
);

CREATE INDEX idx_p2p_rating_rated_user ON p2p_ratings (rated_user_id);
CREATE INDEX idx_p2p_rating_created_at ON p2p_ratings (created_at);
CREATE INDEX idx_p2p_rating_trade ON p2p_ratings (trade_id);