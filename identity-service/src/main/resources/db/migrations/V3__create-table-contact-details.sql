CREATE TABLE IF NOT EXISTS "contact_details" (
    identity_id UUID UNIQUE NOT NULL
        PRIMARY KEY REFERENCES identity(id),
    created_by UUID NOT NULL default '00000000-0000-0000-0000-000000000000',
    email VARCHAR(100) NOT NULL,
    phone_number varchar(20) NOT NULL
);