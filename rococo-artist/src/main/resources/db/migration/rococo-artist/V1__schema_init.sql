CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public."artist"
(
    id        UUID UNIQUE         NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    name      VARCHAR(255) UNIQUE NOT NULL,
    biography VARCHAR(2000)       NOT NULL,
    photo     BYTEA               NOT NULL
);
