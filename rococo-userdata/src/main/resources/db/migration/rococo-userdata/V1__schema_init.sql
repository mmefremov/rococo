CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public."user"
(
    id         UUID UNIQUE        NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_name  VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    avatar     BYTEA
);
