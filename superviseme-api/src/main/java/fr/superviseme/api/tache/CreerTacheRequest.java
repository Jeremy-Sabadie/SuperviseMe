package fr.superviseme.api.tache;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreerTacheRequest(
        @NotBlank @Size(max = 200) String titre,
        UUID dossierId,
        @NotNull UUID responsableId
) {
}
