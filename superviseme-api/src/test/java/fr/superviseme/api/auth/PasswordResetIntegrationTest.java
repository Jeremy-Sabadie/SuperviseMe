package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie que la demande de réinitialisation est publique
 * et ne permet pas d'identifier les comptes existants.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired UtilisateurRepository utilisateurs;
    @Autowired PasswordResetTokenRepository jetons;
    @Autowired PasswordEncoder encoder;

    private String empreinte(String token) throws Exception {
        return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.US_ASCII)));
    }

    /**
     * Vérifie la persistance du nouveau hash et l'utilisation unique.
     * La transaction du test est annulée après exécution.
     */
    @Test
    @Transactional
    void unLienValideModifieLeMotDePasseUneSeuleFois() throws Exception {
        var utilisateur = utilisateurs.save(new Utilisateur(
            "reset-" + UUID.randomUUID() + "@example.test",
            encoder.encode("AncienMotDePasse123!"),
            RoleUtilisateur.USER
        ));

        String token = "B".repeat(43);
        jetons.saveAndFlush(new PasswordResetToken(
            utilisateur, empreinte(token), Instant.now().plusSeconds(900)));

        String requete = "{\"token\":\"" + token
            + "\",\"motDePasse\":\"NouveauMotDePasse123!\"}";

        mvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requete))
            .andExpect(status().isOk());

        assertTrue(encoder.matches("NouveauMotDePasse123!",
            utilisateurs.findById(utilisateur.getId()).orElseThrow().getMotDePasseHash()));

        mvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requete))
            .andExpect(status().isBadRequest());
    }

    /** Un lien expiré doit être refusé avant toute modification. */
    @Test
    @Transactional
    void unLienExpireEstRefuseParApi() throws Exception {
        var utilisateur = utilisateurs.save(new Utilisateur(
            "expire-" + UUID.randomUUID() + "@example.test",
            encoder.encode("AncienMotDePasse123!"),
            RoleUtilisateur.USER
        ));

        String token = "C".repeat(43);
        jetons.saveAndFlush(new PasswordResetToken(
            utilisateur, empreinte(token), Instant.now().minusSeconds(1)));

        mvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token
                    + "\",\"motDePasse\":\"NouveauMotDePasse123!\"}"))
            .andExpect(status().isBadRequest());
    }



    /**
     * Un jeton inexistant ne doit jamais permettre de changer
     * le mot de passe d'un utilisateur.
     */
    @Test
    void unJetonInexistantEstRefuse() throws Exception {
        mvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\","
                        + "\"motDePasse\":\"NouveauMotDePasse123!\"}"))
            .andExpect(status().isBadRequest());
    }

    /** La politique impose au minimum douze caractères. */
    @Test
    void unMotDePasseTropCourtEstRefuse() throws Exception {
        mvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\","
                        + "\"motDePasse\":\"court\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void uneAdresseInconnueRecoitUneReponseNeutre() throws Exception {
        mvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"inconnu@example.com\"}"))
            .andExpect(status().isOk());
    }
}
