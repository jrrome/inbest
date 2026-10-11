# Endpoints 

### `GET /api/users/{id}`
Consultar la información pública de un usuario

### `GET /api/users/{id}`
Consultar la información pública de un usuario

Por el momento solo se devuelve el nombre de 
usuario como información pública, pero otros
detalles como direcciones de sus wallets,
patrimonio estimado, etc. (información que
hay que extraer de otras tablas) también
se podrían añadir.

### `GET /api/users/{id}/orders`
Ver las órdenes de un usuario.

Con esto surge una duda: si solo cada usuario
puede ver sus propias órdenes (habría que
implementar RLS o algo así) o si existe opción
de que un usuario tenga sus órdenes en
público/privado (habría que añadir lógica
adicional para permitirlo).

### `POST /api/users`
Crear un nuevo usuario

### `PATCH /api/users`
Cambiar información de un nuevo usuario

### `GET /api/me`
Esta es una elección común de endpoint 
para consultar la información privada
del propio usuario

### `GET /api/assets/{id}`
Consultar la información de un asset

De assets no creo que sean necesarios más 
endpoints porque el resto de acciones se 
pueden realizar dentro del backend.

### `GET /api/users/{id}/wallets`
Consultar las carteras que tiene un usuario

Creo que la forma más lógica de acceder
a las wallets es mediante el usuario
que la posea.

### `GET /api/users/{id}/wallets/{id}`
Consultar una cartera específica de un usuario

### `POST /api/users/{id}/wallets`
Crear una wallet para un usuario

### `GET /api/`