-- Keep fiat balances at 2 decimal places while allowing crypto balances
-- and ledger values to retain up to 8 decimal places.
ALTER TABLE wallets
    ALTER COLUMN available_balance TYPE NUMERIC(30,8) USING available_balance,
    ALTER COLUMN locked_balance TYPE NUMERIC(30,8) USING locked_balance;

ALTER TABLE ledger_entries
    ALTER COLUMN amount TYPE NUMERIC(30,8) USING amount,
    ALTER COLUMN balance_before TYPE NUMERIC(30,8) USING balance_before,
    ALTER COLUMN balance_after TYPE NUMERIC(30,8) USING balance_after;

-- Existing fiat values remain represented at two decimal places by the
-- application-level currency precision rules (USD and ZWG).
