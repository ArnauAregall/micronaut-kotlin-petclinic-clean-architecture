ALTER TABLE identity
ADD COLUMN created_by uuid NOT NULL default '00000000-0000-0000-0000-000000000000';