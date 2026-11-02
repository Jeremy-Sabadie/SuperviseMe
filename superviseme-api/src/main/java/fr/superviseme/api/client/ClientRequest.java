package fr.superviseme.api.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données validées pour créer ou modifier un client.
 */
public record ClientRequest(
        @NotBlank @Size(max = 150) String nom,
        @Size(max = 150) String entreprise,
        @Email @Size(max = 254) String email,
        @Size(max = 30) String telephone,
        String adresse
) {
}
