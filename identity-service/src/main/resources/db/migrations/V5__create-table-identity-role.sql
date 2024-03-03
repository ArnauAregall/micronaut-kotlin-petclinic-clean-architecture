CREATE TABLE IF NOT EXISTS identity_role (
     identity_id UUID NOT NULL,
     role_id UUID NOT NULL,
     PRIMARY KEY (identity_id, role_id),
     CONSTRAINT fk_identity_role_identity_id FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE,
     CONSTRAINT fk_identity_role_role_id FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
);
