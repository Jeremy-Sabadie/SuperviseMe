package fr.superviseme.api.dossier;

import fr.superviseme.api.auth.RoleUtilisateur;
import fr.superviseme.api.auth.Utilisateur;
import fr.superviseme.api.auth.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DossierConsultationIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper json;

    @Test
    void consultationRetourneParticipantsHorsTransaction() throws Exception {
        String email = "consultation-" + UUID.randomUUID() + "@test.local";
        String motDePasse = "MotDePasseTest123!";

        Utilisateur admin = utilisateurs.save(new Utilisateur(
                email,
                passwordEncoder.encode(motDePasse),
                RoleUtilisateur.ADMIN
        ));

        String connexion = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", email,
                        "motDePasse", motDePasse
                ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = json.readTree(connexion).get("accessToken").asText();
        String authorization = "Bearer " + token;

        String creation = mvc.perform(post("/api/dossiers")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "titre", "Test consultation",
                        "responsableId", admin.getId().toString()
                ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = json.readTree(creation).get("id").asText();

        mvc.perform(put("/api/dossiers/{id}/participants", id)
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "participantIds", java.util.List.of(admin.getId())
                ))))
                .andExpect(status().isOk());

        mvc.perform(get("/api/dossiers/{id}", id)
                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantIds[0]")
                        .value(admin.getId().toString()));
    }
}
