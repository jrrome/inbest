CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users
        PRIMARY KEY (id)
);

CREATE TABLE wallets (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL UNIQUE,
    available_cash NUMERIC(20, 8) NOT NULL DEFAULT 0,
    reserved_cash NUMERIC(20, 8) NOT NULL DEFAULT 0,

    CONSTRAINT pk_wallets
        PRIMARY KEY (id),
    CONSTRAINT fk_users
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT ck_wallets_cash
        CHECK (available_cash >= 0 AND reserved_cash >= 0)
);

CREATE TABLE assets (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    -- De momento se mantendrá un precio fijo
    price NUMERIC(20, 8) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_assets
        PRIMARY KEY (id),
    CONSTRAINT ck_assets_price
        CHECK (price > 0)
);

CREATE TABLE positions (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    wallet_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    available_quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,

    CONSTRAINT pk_positions
        PRIMARY KEY (id),
    CONSTRAINT fk_positions_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id),
    CONSTRAINT fk_positions_asset
        FOREIGN KEY (asset_id)
        REFERENCES assets(id),
    CONSTRAINT uq_positions_wallet_asset
        UNIQUE (wallet_id, asset_id),
    CONSTRAINT ck_positions_quantities
        CHECK (available_quantity >= 0 AND reserved_quantity >= 0)
);

CREATE TYPE order_side AS ENUM ('BUY', 'SELL');

CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    -- Compra o venta (BUY/SELL)
    side order_side NOT NULL,
    quantity NUMERIC(20, 8) NOT NULL,
    -- Cantidad ya vendida/comprada
    filled_quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,
    price NUMERIC(20, 8) NOT NULL,
    -- Se mantiene un VARCHAR en lugar de un ENUM porque los
    -- estados de las órdenes todavía no están definidas y puede
    -- que se cambien
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_orders
        PRIMARY KEY (id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_orders_asset
        FOREIGN KEY (asset_id)
        REFERENCES assets(id),
    CONSTRAINT ck_orders_quantity
        CHECK (quantity > 0),
    CONSTRAINT ck_orders_price
        CHECK (price > 0),
    CONSTRAINT ck_orders_filled_quantity
        CHECK (filled_quantity BETWEEN 0 AND quantity)
);

CREATE TABLE trades (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    buy_order_id BIGINT NOT NULL,
    sell_order_id BIGINT NOT NULL,
    quantity NUMERIC(20, 8) NOT NULL,
    -- El precio puede variar, por eso se almacena el precio al
    -- momento que se ejecuta el trade
    price NUMERIC(20, 8) NOT NULL,
    executed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_trades
        PRIMARY KEY (id),
    CONSTRAINT fk_trades_buy_order
        FOREIGN KEY (buy_order_id)
        REFERENCES orders(id),
    CONSTRAINT fk_trades_sell_order
        FOREIGN KEY (sell_order_id)
        REFERENCES orders(id),
    CONSTRAINT ck_trades_quantity
        CHECK (quantity > 0),
    CONSTRAINT ck_trades_price
        CHECK (price > 0),
    CONSTRAINT ck_trades_different_orders
        CHECK (buy_order_id != sell_order_id)
);

CREATE TYPE wallet_transaction_type AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'BUY', 'SELL');

CREATE TABLE wallet_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    wallet_id BIGINT NOT NULL,
    type wallet_transaction_type NOT NULL,
    amount NUMERIC(20, 8) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_wallet_transactions
        PRIMARY KEY (id),
    CONSTRAINT fk_wallet_transactions_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id),
    CONSTRAINT ck_wallet_transactions_amount
        CHECK (amount > 0)
);