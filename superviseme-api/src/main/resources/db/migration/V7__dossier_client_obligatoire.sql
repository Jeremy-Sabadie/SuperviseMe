-- Un dossier doit obligatoirement être rattaché à un client.
ALTER TABLE dossiers
    ALTER COLUMN client_id SET NOT NULL;
