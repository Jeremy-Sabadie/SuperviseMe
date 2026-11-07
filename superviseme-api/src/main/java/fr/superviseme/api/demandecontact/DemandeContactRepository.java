package fr.superviseme.api.demandecontact;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Accès aux demandes commerciales de l'instance courante.
 *
 * Les demandes sont indépendantes des dossiers.
 */
public interface DemandeContactRepository
        extends JpaRepository<DemandeContact, UUID> {
}
