ALTER TABLE vet
ADD COLUMN created_at timestamp NOT NULL default now();