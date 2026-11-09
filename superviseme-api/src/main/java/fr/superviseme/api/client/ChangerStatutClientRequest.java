package fr.superviseme.api.client;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChangerStatutClientRequest(@NotNull UUID statutId) {
}
