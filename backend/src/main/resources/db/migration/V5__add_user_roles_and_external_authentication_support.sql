ALTER TABLE users
ADD COLUMN role VARCHAR(64) NOT NULL DEFAULT 'USER',
ADD COLUMN authentication_id VARCHAR(255) NOT NULL,
ADD CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN')),
ADD CONSTRAINT uq_users_authentication_id UNIQUE (authentication_id);