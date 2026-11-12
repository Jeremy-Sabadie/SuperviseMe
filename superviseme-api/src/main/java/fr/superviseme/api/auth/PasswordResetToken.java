package fr.superviseme.api.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Jeton temporaire de récupération de mot de passe. */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PasswordResetToken() {}

    public PasswordResetToken(
            Utilisateur utilisateur, String tokenHash, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.utilisateur = utilisateur;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public boolean estValide(Instant maintenant) {
        return usedAt == null && maintenant.isBefore(expiresAt);
    }

    /** Empêche toute réutilisation après changement du mot de passe. */
    public void marquerUtilise(Instant maintenant) {
        this.usedAt = maintenant;
    }
}
