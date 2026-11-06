package fr.superviseme.api.dossier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrat d'entrée pour créer un dossier. */
public record CreerDossierRequest(
        @NotBlank @Size(max = 200) String titre,
        String description,
        @jakarta.validation.constraints.NotNull java.util.UUID clientId,
        @jakarta.validation.constraints.NotNull java.util.UUID responsableId
) {
    public CreerDossierRequest(
            String titre, String description, java.util.UUID clientId) {
        this(titre, description, clientId, null);
    }

    public CreerDossierRequest(String titre, String description) {
        this(titre, description, null, null);
    }
}
