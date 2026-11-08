package fr.superviseme.api.tache;

import java.util.UUID;

/**
 * Signale qu'une tâche demandée n'existe pas dans l'instance.
 */
public class TacheIntrouvableException extends RuntimeException {

    public TacheIntrouvableException(UUID id) {
        super("Tâche introuvable");
    }
}
