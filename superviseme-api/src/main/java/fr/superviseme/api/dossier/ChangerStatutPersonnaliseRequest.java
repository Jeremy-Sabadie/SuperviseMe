package fr.superviseme.api.dossier;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChangerStatutPersonnaliseRequest(@NotNull UUID statutId) {}
