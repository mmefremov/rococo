CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public."user"
(
    id                      UUID UNIQUE        NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_name               VARCHAR(50) UNIQUE NOT NULL,
    password                VARCHAR(255)       NOT NULL,
    enabled                 BOOLEAN            NOT NULL DEFAULT TRUE,
    account_non_expired     BOOLEAN            NOT NULL DEFAULT TRUE,
    account_non_locked      BOOLEAN            NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN            NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS public."authority"
(
    id        UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id   UUID        NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (user_id) REFERENCES "user" (id)
);
