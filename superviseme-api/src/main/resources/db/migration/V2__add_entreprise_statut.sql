-- Ajoute le statut de gestion des entreprises au registre central.
-- Les entreprises déjà enregistrées sont initialisées EN_ATTENTE.

ALTER TABLE entreprises
    ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE';

ALTER TABLE entreprises
    ADD CONSTRAINT chk_entreprises_statut
    CHECK (statut IN ('EN_ATTENTE', 'ACTIVE', 'ERREUR'));
