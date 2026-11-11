package fr.superviseme.api.agenda;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record RendezVousRequest(
        @NotBlank @Size(max = 200) String titre,
        @NotNull LocalDate date,
        @NotNull LocalTime heureDebut,
        @NotNull LocalTime heureFin,
        @Size(max = 255) String lieu,
        String description,
        boolean partage,
        UUID dossierId,
        UUID clientId
) {}
