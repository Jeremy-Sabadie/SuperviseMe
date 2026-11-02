package fr.superviseme.api.client;

import java.time.Instant;
import java.util.UUID;

/**
 * Représentation publique d'un client dans l'API.
 */
public record ClientResponse(
        UUID id,
        String nom,
        String entreprise,
        String email,
        String telephone,
        String adresse,
        Instant dateCreation,
        Instant dateModification
) {

    /** Convertit une entité en réponse API. */
    public static ClientResponse depuis(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getNom(),
                client.getEntreprise(),
                client.getEmail(),
                client.getTelephone(),
                client.getAdresse(),
                client.getDateCreation(),
                client.getDateModification()
        );
    }
}
