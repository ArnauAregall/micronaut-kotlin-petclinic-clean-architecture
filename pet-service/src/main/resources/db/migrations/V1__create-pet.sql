CREATE TABLE IF NOT EXISTS "pet" (
     id UUID UNIQUE NOT NULL,
     type VARCHAR(50) NOT NULL,
     name VARCHAR(50) NOT NULL,
     birth_date TIMESTAMP NOT NULL,
     owner_identity_id UUID,
     PRIMARY KEY (id)
);