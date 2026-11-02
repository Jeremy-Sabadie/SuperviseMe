package fr.superviseme.api.dossier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrat d'entrée pour créer un dossier. */
public record CreerDossierRequest(
        @NotBlank @Size(max = 200) String titre,
        String description
) {}
