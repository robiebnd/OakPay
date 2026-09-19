CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_type VARCHAR(20) NOT NULL,
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    outcome VARCHAR(20) NOT NULL,
    ip_address VARCHAR(64),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_user_id, created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action, created_at DESC);
