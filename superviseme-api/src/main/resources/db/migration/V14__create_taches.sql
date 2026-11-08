-- Chaque entreprise possède sa propre base PostgreSQL.
-- Les tâches peuvent être indépendantes ou associées à un dossier.
CREATE TABLE taches (
    id UUID PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    dossier_id UUID,
    responsable_id UUID,
    statut VARCHAR(30) NOT NULL DEFAULT 'NON_COMMENCEE',
    priorite VARCHAR(30) NOT NULL DEFAULT 'NORMALE',
    echeance DATE,
    date_creation TIMESTAMPTZ NOT NULL,
    date_modification TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_taches_titre
        CHECK (length(trim(titre)) > 0),

    CONSTRAINT chk_taches_statut
        CHECK (statut IN ('NON_COMMENCEE', 'EN_COURS', 'TERMINEE')),

    CONSTRAINT chk_taches_priorite
        CHECK (priorite IN ('BASSE', 'NORMALE', 'HAUTE')),

    -- Empêche d'associer une tâche à un dossier inexistant.
    CONSTRAINT fk_taches_dossier
        FOREIGN KEY (dossier_id) REFERENCES dossiers(id),

    -- Empêche d'affecter une tâche à un utilisateur inexistant.
    CONSTRAINT fk_taches_responsable
        FOREIGN KEY (responsable_id) REFERENCES utilisateurs(id)
);

-- Index sur les champs utilisés pour les recherches et filtrages.
CREATE INDEX idx_taches_dossier ON taches(dossier_id);
CREATE INDEX idx_taches_responsable ON taches(responsable_id);
CREATE INDEX idx_taches_statut ON taches(statut);
