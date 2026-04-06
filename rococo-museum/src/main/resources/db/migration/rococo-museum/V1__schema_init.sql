CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public."museum"
(
    id          UUID UNIQUE   NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(1000) NOT NULL,
    city        VARCHAR(255)  NOT NULL,
    photo       BYTEA         NOT NULL,
    country_id  UUID          NOT NULL
);
ALTER TABLE public."museum"
    ADD CONSTRAINT museum_title_city_key UNIQUE (title, city);
