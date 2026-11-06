package fr.superviseme.api.client;

import java.util.UUID;

/** Exception levée lorsqu'un client demandé n'existe pas. */
public class ClientIntrouvableException extends RuntimeException {

    public ClientIntrouvableException(UUID id) {
        super("Client introuvable : " + id);
    }
}
