package fr.superviseme.api.auth;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UtilisateurRepository repository;
    @Autowired PasswordEncoder encoder;

    @Test
    void connexionJwtEtAccesMe() throws Exception {
        String email = "integration-" + UUID.randomUUID() + "@test.local";
        Utilisateur utilisateur = new Utilisateur(
            email,
            encoder.encode("MotDePasseTest123!"),
            RoleUtilisateur.ADMIN
        );
        repository.saveAndFlush(utilisateur);

        String reponse = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                    "email", email,
                    "motDePasse", "MotDePasseTest123!"
                ))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String token = mapper.readTree(reponse).get("accessToken").asText();

        mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles[0]").value("ADMIN"));

        mvc.perform(get("/api/admin/entreprises")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void accesSansTokenRefuse() throws Exception {
        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalideRefuse() throws Exception {
        mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer token.invalide.signature"))
            .andExpect(status().isUnauthorized());
    }
}
