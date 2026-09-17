UPDATE users
SET role = 'ADMIN',
    status = 'ACTIVE',
    enabled = TRUE
WHERE LOWER(email) = LOWER('robsonbnd21@gmail.com');
