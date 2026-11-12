package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;
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
