-- Eventos
CREATE TABLE event_log (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    occurred_at timestamptz NOT NULL DEFAULT NOW(),
    event_type VARCHAR(64) NOT NULL,
    -- null cuando el hecho lo origina el sistema y no un usuario
    actor_user_id BIGINT,
    -- Sin fk: apunta a tablas distintas según el tipo y el registro
    -- Debe sobrevivir aunque la entidad desaparezca
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT,
    tx_hash VARCHAR(66),
    chain_id BIGINT,
    details JSONB NOT NULL DEFAULT '{}',

    CONSTRAINT pk_event_log PRIMARY KEY (id),
    CONSTRAINT fk_event_log_actor FOREIGN KEY (actor_user_id) REFERENCES users(id),
    CONSTRAINT ck_event_log_event_type CHECK (btrim(event_type) <> ''),
    CONSTRAINT ck_event_log_entity_type CHECK (btrim(entity_type) <> ''),
    CONSTRAINT ck_event_log_chain_id CHECK (chain_id IS NULL OR chain_id > 0),
    CONSTRAINT ck_event_log_tx_hash CHECK (tx_hash IS NULL OR tx_hash ~ '^0x[0-9a-f]{64}$'),
    -- un hash solo identifica una transacción dentro de su cadena
    CONSTRAINT ck_event_log_tx_chain CHECK (tx_hash IS NULL OR chain_id IS NOT NULL),
    CONSTRAINT ck_event_log_details CHECK (jsonb_typeof(details) = 'object')
);

-- Impide editar o borrar filas para que el historial no se pueda reescribir
CREATE FUNCTION reject_event_log_changes() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'event_log es de solo inserción';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_event_log_append_only
    BEFORE UPDATE OR DELETE ON event_log
    FOR EACH ROW EXECUTE FUNCTION reject_event_log_changes();
