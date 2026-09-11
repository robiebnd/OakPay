-- Pending registrations now live outside the users table.
-- Any users that existed before this change were already real accounts and
-- must remain able to sign in after email verification was moved to the
-- pending-registration flow.
UPDATE users
SET email_verified = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE email_verified = FALSE;
