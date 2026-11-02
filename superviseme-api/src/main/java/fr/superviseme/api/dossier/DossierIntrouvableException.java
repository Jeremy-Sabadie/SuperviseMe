package fr.superviseme.api.dossier;

import java.util.UUID;

public class DossierIntrouvableException extends RuntimeException {
    public DossierIntrouvableException(UUID id) {
        super("Dossier introuvable : " + id);
    }
}
