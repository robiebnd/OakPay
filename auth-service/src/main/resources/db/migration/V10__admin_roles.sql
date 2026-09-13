ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(30) NOT NULL DEFAULT 'CLIENT';

CREATE INDEX IF NOT EXISTS idx_users_role
    ON users(role);

CREATE TABLE IF NOT EXISTS client_queries (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    subject VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    description TEXT NOT NULL,
    resolution TEXT,
    assigned_admin_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_client_queries_status
    ON client_queries(status);

CREATE INDEX IF NOT EXISTS idx_client_queries_user
    ON client_queries(user_id);

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id UUID PRIMARY KEY,
    admin_user_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_created
    ON admin_audit_logs(created_at DESC);