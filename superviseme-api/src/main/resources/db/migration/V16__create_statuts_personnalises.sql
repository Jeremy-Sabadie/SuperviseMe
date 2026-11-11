-- Statuts personnalisables propres à chaque instance cliente.
-- L'identifiant reste stable lors des changements de libellé.
CREATE TABLE statuts_personnalises (
    id UUID PRIMARY KEY,
    categorie VARCHAR(40) NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    couleur VARCHAR(7) NOT NULL,
    ordre_affichage INTEGER NOT NULL,

    CONSTRAINT chk_statut_ordre
        CHECK (ordre_affichage >= 0),

    CONSTRAINT chk_statut_categorie
        CHECK (categorie IN (
            'CLIENT',
            'SIGNATURE_COMMERCIALE',
            'APRES_SIGNATURE'
        ))
);

-- Accélère la récupération des statuts dans l'ordre choisi.
CREATE INDEX idx_statuts_categorie_ordre
    ON statuts_personnalises(categorie, ordre_affichage);
