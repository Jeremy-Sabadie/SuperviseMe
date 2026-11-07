-- Empêche deux clients d'avoir le même email dans une instance.
-- Normalisation insensible à la casse et aux espaces périphériques.
-- Les emails NULL restent autorisés.
CREATE UNIQUE INDEX uq_clients_email_normalise
    ON clients (lower(trim(email)))
    WHERE email IS NOT NULL;
