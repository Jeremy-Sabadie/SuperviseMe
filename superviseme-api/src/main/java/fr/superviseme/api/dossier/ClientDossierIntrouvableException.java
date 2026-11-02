package fr.superviseme.api.dossier;

import java.util.UUID;

/**
 * Signale une tentative d'association avec un client inexistant.
 */
public class ClientDossierIntrouvableException extends RuntimeException {

    public ClientDossierIntrouvableException(UUID clientId) {
        super("Client introuvable : " + clientId);
    }
}
