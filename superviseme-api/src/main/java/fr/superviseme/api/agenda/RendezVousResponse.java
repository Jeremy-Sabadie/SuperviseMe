package fr.superviseme.api.agenda;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record RendezVousResponse(
        UUID id,
        String titre,
        LocalDate date,
        LocalTime heureDebut,
        LocalTime heureFin,
        String lieu,
        String description,
        boolean partage,
        UUID dossierId,
        UUID clientId
) {
    public static RendezVousResponse depuis(RendezVous rdv) {
        return new RendezVousResponse(
                rdv.getId(), rdv.getTitre(), rdv.getDate(),
                rdv.getHeureDebut(), rdv.getHeureFin(),
                rdv.getLieu(), rdv.getDescription(),
                rdv.isPartage(), rdv.getDossierId(), rdv.getClientId()
        );
    }
}
