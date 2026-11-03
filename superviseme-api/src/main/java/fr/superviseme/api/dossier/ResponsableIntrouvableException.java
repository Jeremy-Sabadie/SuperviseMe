package fr.superviseme.api.dossier;

import java.util.UUID;

/** Signale une affectation vers un responsable inexistant. */
public class ResponsableIntrouvableException extends RuntimeException {

    public ResponsableIntrouvableException(UUID id) {
        super("Responsable introuvable : " + id);
    }
}
