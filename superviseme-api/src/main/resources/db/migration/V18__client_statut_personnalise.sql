ALTER TABLE clients
    ADD COLUMN statut_personnalise_id UUID;

ALTER TABLE clients
    ADD CONSTRAINT fk_clients_statut_personnalise
    FOREIGN KEY (statut_personnalise_id)
    REFERENCES statuts_personnalises(id)
    ON DELETE RESTRICT;

CREATE INDEX idx_clients_statut_personnalise
    ON clients(statut_personnalise_id);
