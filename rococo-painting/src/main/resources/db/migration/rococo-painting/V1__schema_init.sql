CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public."painting"
(
    id          UUID UNIQUE   NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(1000) NOT NULL,
    artist_id   UUID          NOT NULL,
    museum_id   UUID,
    content     BYTEA         NOT NULL
);
ALTER TABLE public."painting"
    ADD CONSTRAINT painting_title_artist_key UNIQUE (title, artist_id);
