CREATE TABLE IF NOT EXISTS "identity" (
    id UUID UNIQUE NOT NULL,
    first_name VARCHAR(250) NOT NULL,
    last_name VARCHAR(250) NOT NULL,
    PRIMARY KEY (id)
);