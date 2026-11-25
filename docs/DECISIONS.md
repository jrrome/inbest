# Decisiones de diseño

Registro breve de las decisiones cerradas. Cada una indica qué se decidió y por qué.

## 1. Efectivo

El efectivo es EUR simulado y se guarda en `cash_accounts`. No hay token SEUR.

- Por qué: operar con euros en la base de datos es más simple que mantener un token estable y su paridad.

## 2. Precio de referencia

`assets.price` es el precio de referencia. El admin fija el precio de emisión y puede corregirlo.

- Cada trade de la plataforma lo ajusta según `price_impact` (k) y la emisión inicial (Q₀):
  - si la plataforma compra q: precio × (1 − k·q/Q₀)
  - si la plataforma vende q: precio × (1 + k·q/Q₀)
- Se muestran el precio de referencia, el último trade y el mejor bid/ask. Las posiciones se valoran al precio de referencia.

## 3. Mercado

Libro de órdenes entre usuarios, con la plataforma como creador de mercado permanente.

- Al crear un activo, toda la emisión va a la wallet del sistema.
- Las órdenes de la plataforma son órdenes normales del libro, a ±spread/2 del precio de referencia:
  - ask = precio × (1 + spread/2)
  - bid = precio × (1 − spread/2)
- Cada orden de la plataforma está limitada por `max_quote_size` y por su inventario o su efectivo. El efectivo de la plataforma es finito.
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
  - Al darse de alta recibe 0,01 ETH gratis (`inbest.wallet.initial-eth`).
  - Si antes de liquidar una venta su saldo es menor que el mínimo, el admin le recarga hasta 0,01 ETH.
  - La recarga se cobra en euros a un cambio fijo simulado (`inbest.gas.eth-eur-rate`). Si no tiene saldo suficiente, se cobra lo disponible y la plataforma pone el resto.
- Las comisiones y las recargas son movimientos `FEE` de la cuenta de efectivo.
- Los `transfer` de los usuarios se firman con un límite de gas ajustado (unos 100.000), no con `DefaultGasProvider`. Así el saldo de ETH que exige el nodo por adelantado es pequeño.
