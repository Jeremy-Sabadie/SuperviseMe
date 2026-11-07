-- US-010 : demandes commerciales issues du formulaire public.
-- Une demande est liée à un client, mais pas automatiquement à un dossier.

CREATE TABLE demandes_contact (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    motif VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_demandes_contact_client
        FOREIGN KEY (client_id)
        REFERENCES clients(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_demandes_contact_motif
        CHECK (length(trim(motif)) > 0),

    CONSTRAINT chk_demandes_contact_message
        CHECK (length(trim(message)) > 0)
);

CREATE INDEX idx_demandes_contact_client
    ON demandes_contact(client_id);

CREATE INDEX idx_demandes_contact_date_creation
    ON demandes_contact(date_creation);
