CREATE TABLE p2p_disputes (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL UNIQUE,
    opened_by UUID NOT NULL,
    reason VARCHAR(40) NOT NULL,
    evidence TEXT,
    status VARCHAR(20) NOT NULL,
    resolution VARCHAR(20),
    resolution_note TEXT,
    resolved_by UUID,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_p2p_disputes_trade ON p2p_disputes(trade_id);
CREATE INDEX idx_p2p_disputes_status ON p2p_disputes(status);

CREATE TABLE p2p_dispute_audit (
    id UUID PRIMARY KEY,
    dispute_id UUID NOT NULL,
    trade_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    note TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_p2p_dispute_audit_dispute ON p2p_dispute_audit(dispute_id);
CREATE INDEX idx_p2p_dispute_audit_created ON p2p_dispute_audit(created_at);
