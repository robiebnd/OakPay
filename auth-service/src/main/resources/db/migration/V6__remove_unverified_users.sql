DELETE FROM email_verification_tokens
WHERE user_id IN (SELECT id FROM users WHERE email_verified = FALSE);

DELETE FROM password_reset_tokens
WHERE user_id IN (SELECT id FROM users WHERE email_verified = FALSE);

DELETE FROM users
WHERE email_verified = FALSE;
