ALTER TABLE wallets
    ALTER COLUMN available_balance TYPE NUMERIC(20,2) USING ROUND(available_balance, 2),
    ALTER COLUMN locked_balance TYPE NUMERIC(20,2) USING ROUND(locked_balance, 2);

ALTER TABLE ledger_entries
    ALTER COLUMN amount TYPE NUMERIC(20,2) USING ROUND(amount, 2),
    ALTER COLUMN balance_before TYPE NUMERIC(20,2) USING ROUND(balance_before, 2),
    ALTER COLUMN balance_after TYPE NUMERIC(20,2) USING ROUND(balance_after, 2);
