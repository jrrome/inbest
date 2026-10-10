-- Como no había nada en la BD no hay que realizar comprobaciones ni modificar datos

-- Un usuario debe tener un email asociado
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE(email),
    -- email no está vacío y está en minúsculas y sin espacios
    ADD CONSTRAINT ck_users_email_normalized
        CHECK (email != '' AND email = lower(btrim(email)));

-- Añadir la entidad tokens
CREATE TABLE tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    chain_id BIGINT NOT NULL,
    contract_address VARCHAR(42) NOT NULL,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    decimals SMALLINT DEFAULT 18 NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    description TEXT,
    CONSTRAINT pk_tokens PRIMARY KEY (id),
    CONSTRAINT uq_tokens_chain_address UNIQUE (chain_id, contract_address),
    CONSTRAINT ck_tokens_chain_id CHECK (chain_id > 0),
    CONSTRAINT ck_tokens_contract_address
        CHECK (contract_address ~ '^0x[0-9a-f]{40}$'),
    CONSTRAINT ck_tokens_decimals CHECK (decimals BETWEEN 0 AND 255),
    CONSTRAINT ck_tokens_symbol CHECK (btrim(symbol) <> ''),
    CONSTRAINT ck_tokens_name CHECK (btrim(name) <> '')
);

-- Una wallet tiene que estar asociada a una blockchain y tener una dirección
ALTER TABLE wallets
    ADD COLUMN chain_id BIGINT NOT NULL,
    ADD COLUMN address VARCHAR(42) NOT NULL,
    ADD CONSTRAINT uq_wallets_chain_address UNIQUE (chain_id, address),
    ADD CONSTRAINT ck_wallets_chain_id CHECK (chain_id > 0),
    ADD CONSTRAINT ck_wallets_address CHECK (address ~ '^0x[0-9a-f]{40}$');

-- Un asset hace referencia a un token de la blockchain
ALTER TABLE assets
    ADD COLUMN token_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_assets_token FOREIGN KEY (token_id) REFERENCES tokens (id);

-- Representar las cantidades de tokens en NUMERIC(78, 0) (hasta 78 dígitos, suficiente para 256 bits)
ALTER TABLE positions
    ALTER COLUMN available_quantity TYPE NUMERIC(78, 0),
    ALTER COLUMN reserved_quantity TYPE NUMERIC(78, 0);

ALTER TABLE orders
    ALTER COLUMN quantity TYPE NUMERIC(78, 0),
    ALTER COLUMN filled_quantity TYPE NUMERIC(78, 0);

ALTER TABLE trades
    ALTER COLUMN quantity TYPE NUMERIC(78, 0);