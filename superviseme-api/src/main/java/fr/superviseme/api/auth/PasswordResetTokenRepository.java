package fr.superviseme.api.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/** Accès aux empreintes des jetons de réinitialisation. */
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, UUID> {

    @org.springframework.data.jpa.repository.Lock(
        jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query(
        "select t from PasswordResetToken t where t.tokenHash = :hash")
    Optional<PasswordResetToken> verrouillerParHash(
        @org.springframework.data.repository.query.Param("hash") String hash);

    void deleteByUtilisateur_Id(UUID utilisateurId);
}
