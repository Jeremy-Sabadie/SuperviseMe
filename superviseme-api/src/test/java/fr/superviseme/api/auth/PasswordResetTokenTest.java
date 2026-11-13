package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie les règles de validité des jetons.
 * Ces tests ne nécessitent ni SMTP ni base de données.
 */
class PasswordResetTokenTest {

    @Test
    void unJetonExpireEstRefuse() {
        Instant maintenant = Instant.now();

        var jeton = new PasswordResetToken(
            null, "empreinte",
            maintenant.minusSeconds(1)
        );

        assertFalse(jeton.estValide(maintenant));
    }

    @Test
    void unJetonDejaUtiliseEstRefuse() {
        Instant maintenant = Instant.now();

        var jeton = new PasswordResetToken(
            null, "empreinte",
            maintenant.plusSeconds(900)
        );

        assertTrue(jeton.estValide(maintenant));

        jeton.marquerUtilise(maintenant);

        assertFalse(jeton.estValide(maintenant));
    }
}
