-- Jetons de récupération de mot de passe à usage unique.
-- Seule l'empreinte SHA-256 du jeton est conservée en base.
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    utilisateur_id UUID NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Accélère la recherche et la révocation des jetons d'un utilisateur.
CREATE INDEX idx_password_reset_utilisateur
    ON password_reset_tokens(utilisateur_id);
