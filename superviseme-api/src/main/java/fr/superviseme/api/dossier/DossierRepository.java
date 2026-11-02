package fr.superviseme.api.dossier;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DossierRepository extends JpaRepository<Dossier, UUID> {

    /** Vérifie si un client possède au moins un dossier. */
    boolean existsByClientId(UUID clientId);
}
