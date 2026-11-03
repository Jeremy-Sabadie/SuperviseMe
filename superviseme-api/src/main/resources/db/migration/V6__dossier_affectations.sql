-- US-005.1 : responsables et participants des dossiers.
-- Les dossiers historiques peuvent conserver un responsable NULL.

-- Empêche d'affecter un responsable inexistant.
ALTER TABLE dossiers
    ADD CONSTRAINT fk_dossiers_responsable
    FOREIGN KEY (responsable_id)
    REFERENCES utilisateurs(id)
    ON DELETE RESTRICT;

-- Association entre dossiers et participants.
CREATE TABLE dossier_participants (
    dossier_id UUID NOT NULL,
    utilisateur_id UUID NOT NULL,

    CONSTRAINT pk_dossier_participants
        PRIMARY KEY (dossier_id, utilisateur_id),

    CONSTRAINT fk_dossier_participants_dossier
        FOREIGN KEY (dossier_id)
        REFERENCES dossiers(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_dossier_participants_utilisateur
        FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateurs(id)
        ON DELETE RESTRICT
);

-- Accélère la recherche des dossiers d'un participant.
CREATE INDEX idx_dossier_participants_utilisateur
    ON dossier_participants(utilisateur_id);
