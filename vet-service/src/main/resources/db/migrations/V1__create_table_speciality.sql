CREATE TABLE IF NOT EXISTS "speciality" (
    id UUID NOT NULL,
    name VARCHAR NOT NULL UNIQUE,
    description VARCHAR,
    PRIMARY KEY (id)
);