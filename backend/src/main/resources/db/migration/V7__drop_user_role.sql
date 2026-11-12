-- el rol lo decide keycloak a traves de realm_access.roles del token
ALTER TABLE users
DROP CONSTRAINT ck_users_role,
DROP COLUMN role;
