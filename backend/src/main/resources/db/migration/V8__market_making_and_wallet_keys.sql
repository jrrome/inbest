-- Quitar desnormalización
ALTER TABLE assets
    DROP COLUMN name,
    DROP COLUMN symbol;

ALTER TABLE tokens
    ADD CONSTRAINT uq_tokens_chain_symbol UNIQUE (chain_id, symbol);

ALTER TABLE assets
    ADD COLUMN initial_supply NUMERIC(78, 0) NOT NULL,
    ADD COLUMN spread NUMERIC(6, 4) NOT NULL DEFAULT 0.02,
    ADD COLUMN price_impact NUMERIC(6, 4) NOT NULL DEFAULT 0.5,
    ADD COLUMN max_quote_size NUMERIC(78, 0) NOT NULL,
    ADD CONSTRAINT uq_assets_token UNIQUE (token_id),
    ADD CONSTRAINT ck_assets_initial_supply CHECK (initial_supply > 0),
    ADD CONSTRAINT ck_assets_spread CHECK (spread > 0 AND spread < 1),
    ADD CONSTRAINT ck_assets_price_impact CHECK (price_impact >= 0),
    ADD CONSTRAINT ck_assets_max_quote_size CHECK (max_quote_size > 0);

-- Cifrado en sobre: la clave privada va cifrada con una DEK propia y la DEK con la clave maestra.
-- La wallet del sistema firma con la clave admin de la configuración y deja los tres campos a null
ALTER TABLE wallets
    ADD COLUMN encrypted_private_key TEXT,
    ADD COLUMN encrypted_dek TEXT,
    ADD COLUMN kek_version SMALLINT,
    ADD CONSTRAINT ck_wallets_key_material CHECK (
        (encrypted_private_key IS NULL AND encrypted_dek IS NULL AND kek_version IS NULL)
        OR (encrypted_private_key IS NOT NULL AND encrypted_dek IS NOT NULL AND kek_version > 0)
    );

-- Efectivo que le queda reservado a cada orden de compra, para liberar el resto exacto al terminar
ALTER TABLE orders
    ADD COLUMN reserved_cash NUMERIC(20, 8) NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_orders_reserved_cash CHECK (reserved_cash >= 0),
    ADD CONSTRAINT ck_orders_sell_reserved_cash CHECK (side = 'BUY' OR reserved_cash = 0),
    ADD CONSTRAINT ck_orders_status
        CHECK (status IN ('PENDING', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED'));

