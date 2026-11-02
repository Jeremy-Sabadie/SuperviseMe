package fr.superviseme.api.dossier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Contrat d'entrée pour modifier un dossier. */
public record ModifierDossierRequest(
        @NotBlank @Size(max = 200) String titre,
        String description,
        @NotNull StatutDossier statut,
        @NotNull PrioriteDossier priorite,
        LocalDate echeance,
        java.util.UUID clientId
) {
    public ModifierDossierRequest(
            String titre,
            String description,
            StatutDossier statut,
            PrioriteDossier priorite,
            LocalDate echeance) {
        this(titre, description, statut, priorite, echeance, null);
    }
}
