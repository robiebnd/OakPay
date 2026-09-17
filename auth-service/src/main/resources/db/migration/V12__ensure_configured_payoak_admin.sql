-- Local/development administrator bootstrap.
-- The administrator identity is configured explicitly rather than relying on
-- the first user created or on frontend assumptions.
UPDATE users
SET role = 'ADMIN',
    status = 'ACTIVE',
    enabled = TRUE
WHERE LOWER(email) = LOWER('robsonbnd21@gmail.com');

CREATE INDEX IF NOT EXISTS idx_users_email_role
    ON users(email, role);
