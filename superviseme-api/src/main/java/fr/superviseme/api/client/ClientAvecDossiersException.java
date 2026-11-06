package fr.superviseme.api.client;

import java.util.UUID;

/**
 * Signale qu'un client ne peut pas être supprimé
 * tant que des dossiers lui sont associés.
 */
public class ClientAvecDossiersException extends RuntimeException {

    public ClientAvecDossiersException(UUID clientId) {
        super("Impossible de supprimer ce client : des dossiers lui sont associés.");
    }
}
