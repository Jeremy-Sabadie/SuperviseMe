CREATE TABLE jwt_revocations (
    token_hash VARCHAR(64) PRIMARY KEY,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_jwt_revocations_expires_at
    ON jwt_revocations (expires_at);
