## Decisiones pendientes

Las decisiones cerradas están en [DECISIONS.md](DECISIONS.md).

- Stack del frontend.
- Quitar SEUR de `DeployTokens.s.sol`, que ya no se usa (ver decisión 1).
- ¿La primera recarga de ETH es gratis? Si lo es, hace falta una columna `funded_at` en `wallets`.
- `GET /api/users/{id}` devuelve el perfil sin comprobar quién lo pide: restringirlo a admin o devolver solo datos públicos (ver decisión 9).
- Qué eventos de negocio se registran, además de los de las operaciones en la blockchain (ver decisión 10).
