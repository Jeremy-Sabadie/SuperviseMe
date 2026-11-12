package fr.superviseme.api.dossier;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Représentation d'un dossier exposée par l'API. */
public record DossierResponse(
        UUID id,
        String titre,
        String description,
        StatutDossier statut,
        UUID statutPersonnaliseId,
        PrioriteDossier priorite,
        LocalDate echeance,
        UUID clientId,
        UUID responsableId,
        String responsableNom,
        java.util.Set<UUID> participantIds,
        Instant dateCreation,
        Instant dateModification
) {
    /** Centralise la conversion de l'entité en réponse API. */
    public static DossierResponse depuis(
            Dossier dossier, String responsableNom) {
        return new DossierResponse(
                dossier.getId(),
                dossier.getTitre(),
                dossier.getDescription(),
                dossier.getStatut(),
                dossier.getStatutPersonnaliseId(),
                dossier.getPriorite(),
                dossier.getEcheance(),
                dossier.getClientId(),
                dossier.getResponsableId(),
                responsableNom,
                dossier.getParticipantIds(),
                dossier.getDateCreation(),
                dossier.getDateModification()
        );
    }
}
