ALTER TABLE demandes_contact
ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'A_TRAITER';

ALTER TABLE demandes_contact
ADD CONSTRAINT chk_demandes_contact_statut
CHECK (statut IN ('A_TRAITER', 'EN_COURS', 'TRAITE'));

CREATE INDEX idx_demandes_contact_statut
ON demandes_contact(statut);
