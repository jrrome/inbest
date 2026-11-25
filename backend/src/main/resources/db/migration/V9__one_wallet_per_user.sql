ALTER TABLE wallets
    ADD COLUMN name VARCHAR(100),
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD CONSTRAINT uq_wallets_user UNIQUE (user_id);

DROP INDEX ix_wallets_user;
