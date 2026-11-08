package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Vérifie que la désactivation invalide immédiatement les accès API.
 * Un JWT signé et non expiré ne doit pas suffire à contourner le blocage.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UtilisateurDesactivationSecuriteIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UtilisateurRepository repository;
    @Autowired PasswordEncoder encoder;

    @Test
    void jwtExistantEstRefuseApresDesactivation() throws Exception {
        String email = UUID.randomUUID() + "@test.local";

        Utilisateur utilisateur = repository.saveAndFlush(
                new Utilisateur(
                        email,
                        encoder.encode("MotDePasseTest123!"),
                        RoleUtilisateur.USER
                )
        );

        String reponse = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email", email,
                        "motDePasse", "MotDePasseTest123!"
                ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = mapper.readTree(reponse)
                .get("accessToken").asText();

        // Le JWT permet initialement d'accéder au compte.
        mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // La désactivation doit rendre ce même JWT inutilisable.
        utilisateur.desactiver();
        repository.saveAndFlush(utilisateur);

        mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
