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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UtilisateurControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UtilisateurRepository repository;
    @Autowired PasswordEncoder encoder;

    private String token(RoleUtilisateur role) throws Exception {
        String email = UUID.randomUUID() + "@test.local";
        repository.saveAndFlush(new Utilisateur(
            email, encoder.encode("MotDePasseTest123!"), role
        ));

        String reponse = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                    "email", email,
                    "motDePasse", "MotDePasseTest123!"
                ))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        return mapper.readTree(reponse).get("accessToken").asText();
    }

    @Test
    void adminCreeCollaborateur() throws Exception {
        String email = UUID.randomUUID() + "@test.local";

        mvc.perform(post("/api/utilisateurs")
                .header("Authorization", "Bearer " + token(RoleUtilisateur.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                    "email", email,
                    "motDePasse", "MotDePasseCollaborateur123!"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.motDePasseHash").doesNotExist());

        Utilisateur cree = repository.findByEmail(email).orElseThrow();
        assertEquals(RoleUtilisateur.USER, cree.getRole());
        assertTrue(encoder.matches("MotDePasseCollaborateur123!", cree.getMotDePasseHash()));
    }

    @Test
    void collaborateurNePeutPasCreerUtilisateur() throws Exception {
        mvc.perform(post("/api/utilisateurs")
                .header("Authorization", "Bearer " + token(RoleUtilisateur.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                    "email", UUID.randomUUID() + "@test.local",
                    "motDePasse", "MotDePasseCollaborateur123!"
                ))))
            .andExpect(status().isForbidden());
    }

    @Test
    void visiteurNePeutPasCreerUtilisateur() throws Exception {
        mvc.perform(post("/api/utilisateurs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                    "email", UUID.randomUUID() + "@test.local",
                    "motDePasse", "MotDePasseCollaborateur123!"
                ))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminNePeutPasAttribuerRoleAdmin() throws Exception {
        String email = UUID.randomUUID() + "@test.local";

        mvc.perform(post("/api/utilisateurs")
                .header("Authorization", "Bearer " + token(RoleUtilisateur.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                    "email", email,
                    "motDePasse", "MotDePasseCollaborateur123!",
                    "role", "ADMIN"
                ))))
            .andExpect(status().isBadRequest());

        assertFalse(repository.existsByEmail(email));
    }
}
