package fr.superviseme.api.entreprise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Couche d'accès aux données des entreprises.
 *
 * Spring Data JPA fournit les opérations de persistance
 * sans nécessiter d'implémentation manuelle.
 *
 * Attention : ce repository ne contrôle pas les autorisations
 * d'accès entre entreprises.
 */
public interface EntrepriseRepository extends JpaRepository<Entreprise, UUID> {
}
