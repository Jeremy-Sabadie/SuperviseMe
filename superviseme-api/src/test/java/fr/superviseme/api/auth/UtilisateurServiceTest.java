package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie le hachage sécurisé des mots de passe utilisateurs.
 */
class UtilisateurServiceTest {

    @Test
    void doitHacherLeMotDePasseSansLeStockerEnClair() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String motDePasse = "MotDePasseTest!2026";
        String hash = encoder.encode(motDePasse);

        assertThat(hash).isNotEqualTo(motDePasse);
        assertThat(encoder.matches(motDePasse, hash)).isTrue();
        assertThat(encoder.matches("MauvaisMotDePasse", hash)).isFalse();
    }
}
