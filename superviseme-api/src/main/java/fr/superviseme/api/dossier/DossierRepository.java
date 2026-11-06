package fr.superviseme.api.dossier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface DossierRepository extends JpaRepository<Dossier, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Dossier d where d.id = :id")
    Optional<Dossier> findByIdForUpdate(UUID id);

    /** Vérifie si un client possède au moins un dossier. */
    boolean existsByClientId(UUID clientId);
}
