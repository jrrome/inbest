-- El cash debe pertenecer al usuario, no estar asociado a cada wallet
CREATE TABLE cash_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    available_cash NUMERIC(20, 8) NOT NULL DEFAULT 0,
    reserved_cash NUMERIC(20, 8) NOT NULL DEFAULT 0,

    CONSTRAINT pk_cash_accounts PRIMARY KEY (id),
    CONSTRAINT fk_cash_accounts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_cash_accounts_user UNIQUE (user_id),
    CONSTRAINT ck_cash_accounts_currency CHECK (currency = 'EUR'),
    CONSTRAINT ck_cash_accounts_balances CHECK (available_cash >= 0 AND reserved_cash >= 0)
);

ALTER TABLE wallet_transactions RENAME TO cash_transactions;
ALTER TYPE wallet_transaction_type RENAME TO cash_transaction_type;

ALTER TABLE cash_transactions
    DROP CONSTRAINT fk_wallet_transactions_wallet,
    DROP COLUMN wallet_id,
    ADD COLUMN cash_account_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_cash_transactions_account
        FOREIGN KEY (cash_account_id) REFERENCES cash_accounts(id),
    ADD COLUMN trade_id BIGINT,
    ADD CONSTRAINT fk_cash_transactions_trade FOREIGN KEY (trade_id) REFERENCES trades(id),
    ADD CONSTRAINT uq_cash_transactions_trade UNIQUE (cash_account_id, trade_id, type),
    ADD CONSTRAINT ck_cash_transactions_trade_type
        CHECK (
            (type IN ('BUY', 'SELL') AND trade_id IS NOT NULL)
            OR (type IN ('DEPOSIT', 'WITHDRAWAL') AND trade_id IS NULL)
        );

ALTER TABLE cash_transactions RENAME CONSTRAINT pk_wallet_transactions TO pk_cash_transactions;
ALTER TABLE cash_transactions RENAME CONSTRAINT ck_wallet_transactions_amount TO ck_cash_transactions_amount;

ALTER TABLE wallets
    DROP CONSTRAINT wallets_user_id_key,
    DROP CONSTRAINT ck_wallets_cash,
    DROP COLUMN available_cash,
    DROP COLUMN reserved_cash,
    ADD CONSTRAINT uq_wallets_id_user UNIQUE (id, user_id);

ALTER TABLE orders
    ADD COLUMN wallet_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_orders_wallet_user
        FOREIGN KEY (wallet_id, user_id) REFERENCES wallets(id, user_id);

CREATE INDEX ix_wallets_user ON wallets(user_id);
CREATE INDEX ix_orders_wallet ON orders(wallet_id, user_id);

ALTER TABLE trades
    ADD COLUMN settlement_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN chain_id BIGINT NOT NULL,
    ADD COLUMN tx_hash VARCHAR(66),
    ADD COLUMN confirmed_at TIMESTAMPTZ;

ALTER TABLE trades
    ADD CONSTRAINT ck_trades_chain_id CHECK (chain_id > 0),
    ADD CONSTRAINT ck_trades_tx_hash CHECK (tx_hash ~ '^0x[0-9a-f]{64}$'),
    ADD CONSTRAINT uq_trades_chain_tx_hash UNIQUE (chain_id, tx_hash),
    ADD CONSTRAINT ck_trades_settlement CHECK (
        (settlement_status = 'PENDING' AND tx_hash IS NULL AND confirmed_at IS NULL)
        OR (settlement_status = 'SUBMITTED' AND tx_hash IS NOT NULL AND confirmed_at IS NULL)
        OR (settlement_status = 'CONFIRMED' AND tx_hash IS NOT NULL AND confirmed_at IS NOT NULL)
        OR (settlement_status = 'FAILED' AND confirmed_at IS NULL)
    );
