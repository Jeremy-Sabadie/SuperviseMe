package fr.superviseme.api.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/** Accès aux comptes utilisateurs de l'instance. */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {
    boolean existsByEmail(String email);
    Optional<Utilisateur> findByEmail(String email);
}
