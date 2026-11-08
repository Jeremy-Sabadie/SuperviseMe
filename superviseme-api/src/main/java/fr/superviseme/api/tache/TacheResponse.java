package fr.superviseme.api.tache;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Représentation publique d'une tâche dans l'API REST.
 *
 * Évite d'exposer directement l'entité JPA et permet de faire évoluer
 * indépendamment le modèle de persistance et le contrat HTTP.
 */
public record TacheResponse(
        UUID id,
        String titre,
        String description,
        UUID dossierId,
        UUID responsableId,
        StatutTache statut,
        PrioriteTache priorite,
        LocalDate echeance,
        Instant dateCreation,
        Instant dateModification
) {

    /**
     * Convertit une entité métier en réponse HTTP.
     *
     * @param tache entité à représenter
     * @return représentation publique de la tâche
     */
    public static TacheResponse depuis(Tache tache) {
        return new TacheResponse(
                tache.getId(),
                tache.getTitre(),
                tache.getDescription(),
                tache.getDossierId(),
                tache.getResponsableId(),
                tache.getStatut(),
                tache.getPriorite(),
                tache.getEcheance(),
                tache.getDateCreation(),
                tache.getDateModification()
        );
    }
}
