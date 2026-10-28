# Registro e inicio de sesión

El backend permite crear una cuenta y entrar con un enlace de un solo uso enviado
por correo. Spring Security gestiona los tokens y la sesión; PostgreSQL guarda
los usuarios y sus cuentas de efectivo. No hay login por contraseña.

## Arranque local

Requisitos: Docker funcionando y el JDK 25 que utiliza el proyecto.
Desde la raíz del repositorio:

```sh
docker compose up -d postgres mailpit
cd backend
./gradlew bootRun --args='--spring.docker.compose.enabled=false'
```

La opción de arranque evita iniciar el resto de Compose, incluida la blockchain,
que no es necesaria para probar la autenticación. El arranque habitual con
`./gradlew bootRun` también puede gestionar los servicios de Compose.

- Backend: <http://localhost:8080>
- Página de acceso: <http://localhost:8080/login>
- Buzón local de Mailpit: <http://localhost:8025>

La configuración SMTP de desarrollo ya apunta a `localhost:1025`. El remitente
se configura con `inbest.auth.mail-from` y la URL de los enlaces con
`inbest.auth.base-url`; se usa esta última en vez de construir enlaces desde la
cabecera `Host` de la petición.

## Crear una cuenta

El alta es `POST /api/users`, con JSON que contiene `username` y `email`. Recorta
los espacios exteriores de ambos y convierte el correo a minúsculas. El usuario
admite hasta 30 caracteres y el correo hasta 255; ambos son obligatorios. El correo
debe ser válido y no contener espacios. El nombre de usuario distingue mayúsculas.

El cliente debe obtener primero un token CSRF y conservar su cookie de sesión.
Por ejemplo, en la consola del navegador abierta en `http://localhost:8080/login`:

```js
const csrf = await fetch('/api/csrf', { credentials: 'include' })
  .then(response => response.json());

const registration = await fetch('/api/users', {
  method: 'POST',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json',
    [csrf.headerName]: csrf.token
  },
  body: JSON.stringify({ username: 'jaime', email: 'jaime@example.test' })
});
console.log(registration.status, await registration.json());
```

El alta devuelve `201 Created`, un `Location: /api/users/{id}` y el DTO privado
del usuario creado. Crea también una cuenta EUR con efectivo disponible y reservado
a cero. Ambas inserciones forman una transacción: si falla una, se deshacen las dos.
El registro no inicia sesión ni concede acceso antes de demostrar el control del
correo mediante el enlace de acceso.

Los datos inválidos devuelven `400` con `application/problem+json` y un mapa
`errors` por campo. Un usuario o correo duplicado devuelve `409`, también en formato
Problem Detail. Las restricciones únicas de PostgreSQL protegen igualmente frente
a registros simultáneos; otros fallos de integridad no se presentan como duplicados.

## Entrar por correo

1. Abrir `/login` y enviar el nombre de usuario. El formulario envía
   `POST /ott/generate` con el parámetro de formulario `username` y CSRF.
2. La respuesta redirige a `/ott/sent`. Se muestra la misma confirmación para
   cuentas existentes, inexistentes, solicitudes inválidas o limitadas.
3. Si la cuenta existe y se permite el envío, buscar el correo en Mailpit. El
   destinatario es siempre el correo almacenado, no un email recibido en la petición.
4. Abrir el enlace `/login/ott?token=...`. El GET muestra una confirmación sin
   consumir el token; así una previsualización del enlace no inicia sesión.
5. Confirmar el formulario, que envía `POST /login/ott`. Un token válido se consume
   una sola vez, inicia sesión y redirige a `/api/me`.

Los tokens caducan a los cinco minutos. Los tokens inválidos, caducados o ya
consumidos redirigen a `/login?error` y no autentican al usuario. Si falla el SMTP,
se revoca el token, se registra un aviso sin incluir el enlace ni el correo y se
mantiene la confirmación genérica. Comprueba el servicio Mailpit y solicita otro
enlace dentro de los límites de envío.

Se admiten hasta tres solicitudes por nombre de usuario y treinta por dirección
remota en ventanas de cinco minutos. Las solicitudes limitadas no generan tokens
ni envían correos. Los contadores tienen capacidad limitada y se eliminan al
caducar. Se utiliza la dirección de la conexión, sin confiar en cabeceras reenviadas
por el cliente.

## Sesión, privacidad y CSRF

La sesión se mantiene mediante la cookie `JSESSIONID`. El identificador de sesión
se renueva al autenticar. La configuración local establece una caducidad por
inactividad de 30 minutos y `SameSite=Lax`.

- `GET /api/users/{id}` es público y solo devuelve `username`.
- `GET /api/me` devuelve `id`, `username`, `email` y `createdAt` del usuario de la
  sesión. No acepta un ID para consultar datos de otro usuario.
- Las rutas privadas de `/api/**` devuelven `401` sin sesión, sin redirigir a HTML.
- `GET /api/csrf` es público y devuelve `token`, `headerName` y `parameterName`.
- Las peticiones que modifican datos necesitan CSRF, incluido registro, solicitud
  de enlace, consumo del token y logout. Sin él, la respuesta es `403`.

Un cliente JavaScript debe enviar `csrf.token` en la cabecera `csrf.headerName` y
conservar las cookies. Debe volver a llamar a `/api/csrf` después de autenticar o
cerrar sesión, ya que estas operaciones invalidan el token CSRF anterior. Los
formularios generados por Spring ya incluyen el campo CSRF.

Para salir, desde una página del backend:

```js
const logoutCsrf = await fetch('/api/csrf', { credentials: 'include' })
  .then(response => response.json());
await fetch('/logout', {
  method: 'POST',
  credentials: 'include',
  headers: { [logoutCsrf.headerName]: logoutCsrf.token }
});
window.location.assign('/login?logout');
```

El logout invalida la sesión y redirige a `/login?logout`.

El cliente debe usar el mismo origen que el backend, o un proxy de desarrollo
que publique `/api`, `/login`, `/ott` y `/logout` en el mismo origen. Todavía no se
configura acceso CORS para un frontend separado.

## Pruebas y límites del entorno actual

Desde `backend`, ejecutar `./gradlew test`. Los tests arrancan PostgreSQL 18 en
un contenedor temporal con puertos dinámicos, aplican Flyway y sustituyen el envío
SMTP por un doble que captura el mensaje. No necesitan Mailpit, Anvil ni la base
de datos de desarrollo. Incluyen el recorrido completo con CSRF real, privacidad,
duplicados, rollback, fijación de sesión, caducidad, reutilización, límites y fallos SMTP.

Los tokens, las sesiones y los límites se mantienen en memoria de una única
instancia. Reiniciar el backend invalida enlaces pendientes y sesiones y reinicia
los límites. Antes de desplegar varias instancias hará falta almacenamiento
compartido y configurar HTTPS y cookies seguras para el entorno de despliegue.
