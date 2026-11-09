CREATE TABLE realisations (
    id UUID PRIMARY KEY,
    titre VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    type_image VARCHAR(20) NOT NULL,
    image BYTEA NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
