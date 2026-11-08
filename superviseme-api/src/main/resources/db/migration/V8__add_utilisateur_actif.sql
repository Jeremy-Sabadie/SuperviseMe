-- Les comptes existants restent actifs lors de la migration.
-- La désactivation conserve les relations et l'historique métier.
ALTER TABLE utilisateurs
    ADD COLUMN actif BOOLEAN NOT NULL DEFAULT TRUE;
