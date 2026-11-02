CREATE TABLE utilisateurs (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
        CHECK (role IN ('ADMIN', 'USER'))
);
