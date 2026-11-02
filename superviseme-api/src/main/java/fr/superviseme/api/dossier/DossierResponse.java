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
        PrioriteDossier priorite,
        LocalDate echeance,
        UUID clientId,
        Instant dateCreation,
        Instant dateModification
) {
    /** Centralise la conversion de l'entité en réponse API. */
    public static DossierResponse depuis(Dossier dossier) {
        return new DossierResponse(
                dossier.getId(),
                dossier.getTitre(),
                dossier.getDescription(),
                dossier.getStatut(),
                dossier.getPriorite(),
                dossier.getEcheance(),
                dossier.getClientId(),
                dossier.getDateCreation(),
                dossier.getDateModification()
        );
    }
}
