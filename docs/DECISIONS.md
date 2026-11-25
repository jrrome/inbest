# Decisiones de diseño ya definitivas

## 1. Efectivo

El efectivo es EUR simulado y se guarda en `cash_accounts`.

## 2. Precio de referencia

`assets.price` es el precio de referencia. El admin fija el precio de emisión y puede corregirlo.

- Cada trade de la plataforma lo ajusta según `price_impact` (k) y la emisión inicial (Q_0):
  - si la plataforma compra q: precio × (1 − k·q/Q_0)
  - si la plataforma vende q: precio × (1 + k·q/Q_0)
- Se muestran el precio de referencia, el último trade y el mejor bid/ask. Las posiciones se valoran al precio de referencia.

## 3. Mercado

Libro de órdenes entre usuarios, con la plataforma como creador de mercado permanente.

- Al crear un activo, toda la emisión va a la wallet del sistema.
- Las órdenes de la plataforma son órdenes normales del libro, a ±spread/2 del precio de referencia:
  - ask = precio × (1 + spread/2)
  - bid = precio × (1 − spread/2)
- Cada orden de la plataforma está limitada por `max_quote_size` y por su inventario o su efectivo. El efectivo de la 
plataforma es finito.
- La plataforma recotiza después de cada trade suyo.
- Solo hay órdenes límite.
- El emparejamiento se hace al crear la orden, con bloqueo pesimista por activo.
- El trade se ejecuta al precio de la orden que ya estaba en el libro.
- No se permite cruzar órdenes propias.
- Cancelar una orden libera lo que tenía reservado.

## 4. Custodia de claves

La plataforma custodia las claves privadas de los usuarios con cifrado en sobre:

- Cada wallet tiene su DEK, que cifra la clave privada con AES-256-GCM.
- La DEK se cifra con la clave maestra (KEK) indicada en `kek_version`.
- La KEK está en `INBEST_WALLET_MASTER_KEY`, en lugar de un KMS. El valor por defecto solo vale para desarrollo.
- La dirección de la wallet es la AAD.
- Para rotar la KEK solo hay que volver a cifrar las DEK.

La wallet del sistema es la cuenta admin y no guarda clave en la base de datos.

## 5. Liquidación

La transferencia del token la firma el vendedor con un `transfer` ERC-20. No hace falta cambiar el contrato.

## 6. Emisión

El token se mintea entero en el constructor del contrato. Desplegarlo y emitir la supply es una única transacción, así que crear un activo es síncrono.

## 7. Gas

En la blockchain, el gas lo paga en ETH quien firma la transacción. En euros:

- **Compra a la plataforma:** firma el admin. El comprador paga una comisión fija en euros (`inbest.fees.platform-transfer`):
  - se reserva al crear la orden, junto con precio × cantidad;
  - se cobra una vez por orden, en su primer trade contra la plataforma;
  - si la orden nunca opera con la plataforma, se libera.
- **Ventas:** firma el vendedor, que paga el gas con el ETH de su wallet.
  - Al crear la wallet no se le envía ETH (ver decisión 8).
  - Si antes de liquidar una venta su saldo es menor que el mínimo, el admin le recarga hasta 0,01 ETH (`inbest.wallet.initial-eth`). El saldo se consulta en la blockchain; no hay columna.
  - La recarga se cobra en euros a un cambio fijo simulado (`inbest.gas.eth-eur-rate`). Si no tiene saldo suficiente, se cobra lo disponible y la plataforma pone el resto.
- Las comisiones y las recargas son movimientos `FEE` de la cuenta de efectivo.
- Los `transfer` de los usuarios se firman con un límite de gas ajustado (unos 100.000), no con `DefaultGasProvider`. Así el saldo de ETH que exige el nodo por adelantado es pequeño.

## 8. Wallet

La wallet es opcional y cada usuario tiene como mucho una (`uq_wallets_user`).

- Se crea con `POST /api/me/wallet`, no al darse de alta. Así el alta no depende de la blockchain y no se gasta ETH en quien solo mira.
- Crearla no toca la blockchain: la dirección se deriva del par de claves generado en local.
- Las acciones que mueven tokens (crear órdenes) exigen tenerla. Ver el mercado, ingresar euros y consultar saldos, no.
- No hay columna `has_wallet`: tener wallet es tener una fila en `wallets`.

## 9. Rutas

- `/api/me/...` para lo privado del usuario autenticado: el usuario sale del JWT y no se puede pedir lo de otro.
- `/api/users/{id}/...` solo para endpoints de admin (`@PreAuthorize("hasRole('ADMIN')")`) o datos públicos.
- Cada controlador agrupa un recurso, aunque tenga rutas de los dos tipos.

## 10. Diario de operaciones en la blockchain

`event_log` sirve de diario de intenciones (WAL), al estilo de un event sourcing ligero, para que el sistema se recupere solo cuando la blockchain y la base de datos quedan inconsistentes.

- `event_log` es auditoría de negocio. La traza técnica va al log de la aplicación.
- Cada evento hace commit en su propia transacción, así que no lo borra el rollback de la operación principal.
- Los eventos de una misma operación comparten una columna `operation_id` (UUID, con índice). El estado de una operación es su último evento.
- La transacción se firma en local y su hash se calcula antes de enviarla. El evento `*_REQUESTED`, con el `txHash` y la transacción firmada, hace commit antes del envío. Si la base de datos falla ahí, no se toca la blockchain.
- Ejemplo de despliegue: `DEPLOY_REQUESTED` → `DEPLOY_CONFIRMED` (dirección del contrato) → `TOKEN_PERSISTED`, que va en la misma transacción que el token. El final con error es `DEPLOY_FAILED`.
- El nonce lo gestiona la aplicación, con un único hilo que escribe en la blockchain.
- Un proceso periódico busca las operaciones sin evento final y consulta su recibo:
  - con éxito: completa lo que falta en la base de datos;
  - revertida: registra el fallo;
  - sin recibo: vuelve a enviar la misma transacción firmada.

  Nunca firma de nuevo, así que no puede duplicar un despliegue ni una transferencia.
- Sirve para todas las operaciones en la blockchain: despliegues, recargas de ETH y liquidaciones.
- Se hace en dos fases: primero los eventos y el hash calculado antes de enviar (con el ejecutor único); después el proceso de recuperación.
