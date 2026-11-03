package fr.superviseme.api.dossier;

import jakarta.validation.constraints.NotNull;

/** Contrat d'entrée pour déplacer un dossier dans le Kanban. */
public record ChangerStatutDossierRequest(
        @NotNull StatutDossier statut
) {
}
