package fr.superviseme.api.client;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Accès aux clients de l'instance courante.
 */
public interface ClientRepository extends JpaRepository<Client, UUID> {

    /**
     * Recherche les clients par nom, sans distinction de casse.
     */
    List<Client> findByNomContainingIgnoreCaseOrderByNomAsc(String nom);

    /** Retourne les clients triés par nom. */
    List<Client> findAllByOrderByNomAsc();
}
