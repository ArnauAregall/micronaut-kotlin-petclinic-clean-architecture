CREATE TABLE IF NOT EXISTS "vet_speciality" (
    vet_id UUID NOT NULL,
    speciality_id UUID NOT NULL,
    PRIMARY KEY (vet_id, speciality_id),
    FOREIGN KEY (vet_id) REFERENCES vet (id) ON DELETE CASCADE,
    FOREIGN KEY (speciality_id) REFERENCES speciality (id) ON DELETE CASCADE
);