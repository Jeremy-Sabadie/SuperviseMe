ALTER TABLE dossiers ADD COLUMN statut_personnalise_id UUID;
ALTER TABLE dossiers ADD CONSTRAINT fk_dossier_statut_personnalise
FOREIGN KEY (statut_personnalise_id) REFERENCES statuts_personnalises(id)
ON DELETE RESTRICT;
CREATE INDEX idx_dossiers_statut_personnalise ON dossiers(statut_personnalise_id);
