ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(30),
    ADD COLUMN country VARCHAR(100),
    ADD COLUMN date_of_birth DATE;

CREATE INDEX idx_users_phone_number ON users(phone_number);
