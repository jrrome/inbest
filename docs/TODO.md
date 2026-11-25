# TO DO

Por orden de prioridad. Las decisiones están en [DECISIONS.md](DECISIONS.md)

## Urgente

- [ ] Decidir el stack del frontend
- [ ] Frontend: login con Keycloak (PKCE), cartera, efectivo, wallet, mercado y órdenes

## Backend: camino de la demo

- [ ] Ejecutor único de blockchain (un hilo, nonce propio)
- [ ] Separar el despliegue: `TokenService.deploy` devuelve `DeployedToken` sin tocar la BD
- [ ] Crear activo: `POST /api/assets` (token, activo, posición del sistema, órdenes)
- [ ] Recotizar las órdenes de la plataforma
- [ ] Consultar activos: `GET /api/assets` y `/{id}` con bid/ask y último trade
- [ ] Crear órdenes con emparejamiento (reservas, trades, `cash_transactions`, precio de referencia)
- [ ] Exigir wallet para crear órdenes (409 `WALLET_REQUIRED`)
- [ ] Comisión fija por orden contra la plataforma y tipo `FEE` (migración)
- [ ] Cancelar órdenes
- [ ] Liquidación: cola de trades, `transfer` firmado por el vendedor, estado del trade
- [ ] Recarga de ETH antes de vender, cobrada en euros

## Backend: después

- [ ] WAL fase 1: `operation_id`, eventos en su propia transacción, firmar y calcular el hash antes de enviar
- [ ] WAL fase 2: proceso que recupera las operaciones sin terminar
- [ ] Eventos del despliegue: solicitado, confirmado, fallido (`TokenService`)
- [ ] Decidir qué otros eventos de negocio se registran
- [ ] Quitar el comentario "para continuar…" de `TokenService`
- [ ] `GET /api/tokens/{id}`, ahora comentado en `TokenController`
- [ ] `GET /api/users/{id}`: solo admin o solo datos públicos
- [ ] `dto.Event.from` falla si el evento no tiene actor
- [ ] Revisar `@Valid` y el `ExceptionHandler` en `UserController`
- [ ] Quitar SEUR de `DeployTokens.s.sol`

## Por decidir

- [ ] ¿La primera recarga de ETH es gratis? (columna `funded_at`)
