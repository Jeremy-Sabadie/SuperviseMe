-- US-011 : évolution du cycle commercial des demandes.
-- Conservation des anciens statuts pour préserver les données existantes.

ALTER TABLE demandes_contact
    ALTER COLUMN statut TYPE VARCHAR(50);

ALTER TABLE demandes_contact
    DROP CONSTRAINT chk_demandes_contact_statut;

ALTER TABLE demandes_contact
    ADD CONSTRAINT chk_demandes_contact_statut
    CHECK (statut IN (
        'A_TRAITER',
        'EN_COURS',
        'TRAITE',
        'PROPOSITION_COMMERCIALE_A_ETABLIR',
        'PROPOSITION_COMMERCIALE_ENVOYEE',
        'PROPOSITION_COMMERCIALE_ACCEPTEE',
        'PROPOSITION_COMMERCIALE_REFUSEE'
    ));
