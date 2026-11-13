CREATE TABLE rendez_vous (
    id UUID PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    date DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    lieu VARCHAR(255),
    description TEXT,
    partage BOOLEAN NOT NULL DEFAULT FALSE,
    dossier_id UUID,
    client_id UUID,
    CONSTRAINT chk_rendez_vous_horaires
        CHECK (heure_fin > heure_debut)
);

CREATE INDEX idx_rendez_vous_date
    ON rendez_vous(date);
