# Endpoints

## Implementados

| Método | Ruta | Acceso | Función |
| --- | --- | --- | --- |
| GET | `/api/csrf` | Público | Obtener CSRF y establecer la sesión del cliente. |
| POST | `/api/users` | Público, con CSRF | Registrar usuario y crear su cuenta EUR con saldo cero. |
| GET | `/api/users/{id}` | Público | Consultar únicamente el nombre público de usuario. |
| GET | `/api/me` | Sesión | Consultar los datos privados del usuario autenticado. |
| GET | `/login` | Público | Formulario de solicitud de enlace. |
| POST | `/ott/generate` | Público, con CSRF | Solicitar un enlace para `username` mediante formulario. |
| GET | `/ott/sent` | Público | Confirmación genérica de la solicitud. |
| GET | `/login/ott` | Público | Formulario de confirmación del token. |
| POST | `/login/ott` | Público, con CSRF | Consumir `token` e iniciar sesión. |
| POST | `/logout` | Con CSRF | Invalidar la sesión. |

El alta acepta JSON con `username` y `email`. Devuelve `201 Created`, la cabecera
`Location: /api/users/{id}` y el DTO privado del usuario. Devuelve `400` por datos
inválidos y `409` por nombre o correo duplicados, en formato Problem Detail.

La consulta pública devuelve `404` si no existe el ID. `/api/me` devuelve `id`,
`username`, `email` y `createdAt`, o `401` sin sesión. Las demás rutas privadas
de la API también requieren sesión. Las peticiones que modifican datos sin un
CSRF válido devuelven `403`, aunque la ruta permita usuarios anónimos.

El flujo de acceso, la configuración local, los límites de envío y ejemplos
de cliente se describen en [AUTHENTICATION.md](AUTHENTICATION.md).

## Propuestas pendientes

Estas rutas todavía no tienen controlador ni lógica de negocio:

- `PATCH /api/users`: actualizar los datos propios; el contrato está por definir.
- `GET /api/users/{id}/orders`: consultar órdenes. Pendiente decidir su visibilidad;
  las consultas privadas deberán comprobar que pertenecen al usuario de la sesión.
- `GET /api/assets/{id}`: consultar un activo.
- `GET /api/users/{id}/wallets`: consultar las carteras del usuario.
- `GET /api/users/{id}/wallets/{walletId}`: consultar una cartera concreta.
- `POST /api/users/{id}/wallets`: crear una cartera, verificando su propietario.

También quedan por definir la creación de órdenes, las reservas de efectivo o
tokens y su ejecución y liquidación.
