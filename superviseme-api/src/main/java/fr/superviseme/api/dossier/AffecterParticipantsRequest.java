package fr.superviseme.api.dossier;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

/**
 * Liste des utilisateurs participant à un dossier.
 * Une liste vide permet de retirer toutes les affectations.
 */
public record AffecterParticipantsRequest(
        @NotNull Set<@NotNull UUID> participantIds
) {
}
