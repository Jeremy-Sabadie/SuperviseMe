CREATE TABLE dossiers (
    id UUID PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    statut VARCHAR(30) NOT NULL DEFAULT 'A_TRAITER',
    priorite VARCHAR(30) NOT NULL DEFAULT 'NORMALE',
    client_id UUID,
    responsable_id UUID,
    echeance DATE,
    date_creation TIMESTAMPTZ NOT NULL,
    date_modification TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_dossiers_titre
        CHECK (length(trim(titre)) > 0),

    CONSTRAINT chk_dossiers_statut
        CHECK (statut IN ('A_TRAITER', 'EN_COURS', 'TERMINE', 'ARCHIVE')),

    CONSTRAINT chk_dossiers_priorite
        CHECK (priorite IN ('BASSE', 'NORMALE', 'HAUTE'))
);

CREATE INDEX idx_dossiers_statut ON dossiers(statut);
CREATE INDEX idx_dossiers_responsable ON dossiers(responsable_id);
CREATE INDEX idx_dossiers_echeance ON dossiers(echeance);
