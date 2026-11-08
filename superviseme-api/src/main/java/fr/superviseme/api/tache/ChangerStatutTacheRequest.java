package fr.superviseme.api.tache;

import jakarta.validation.constraints.NotNull;

/**
 * Commande de changement de statut d'une tâche.
 *
 * Le responsable et les autres propriétés ne sont pas modifiables
 * par cette opération.
 *
 * @param statut nouveau statut obligatoire
 */
public record ChangerStatutTacheRequest(
        @NotNull StatutTache statut
) {}
