-- Référentiel clients propre à chaque instance SuperviseMe.
CREATE TABLE clients (
    id UUID PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    entreprise VARCHAR(150),
    email VARCHAR(254),
    telephone VARCHAR(30),
    adresse TEXT,
    date_creation TIMESTAMPTZ NOT NULL,
    date_modification TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_clients_nom
        CHECK (length(trim(nom)) > 0)
);

-- Accélère les recherches par nom.
CREATE INDEX idx_clients_nom
    ON clients (lower(nom));

-- Garantit qu'un dossier ne référence pas un client inexistant.
ALTER TABLE dossiers
    ADD CONSTRAINT fk_dossiers_client
    FOREIGN KEY (client_id)
    REFERENCES clients(id)
    ON DELETE RESTRICT;

-- Accélère la recherche des dossiers d'un client.
CREATE INDEX idx_dossiers_client
    ON dossiers(client_id);
