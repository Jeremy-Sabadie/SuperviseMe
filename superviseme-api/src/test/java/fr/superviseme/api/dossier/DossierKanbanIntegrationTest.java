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
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration US-005.2 : déplacement Kanban des dossiers.
 *
 * Vérifie les permissions avec des JWT réels et la persistance PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DossierKanbanIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private UtilisateurRepository utilisateurs;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper json;

    private record Compte(UUID id, String token) {
        String authorization() {
            return "Bearer " + token;
        }
    }

    /** Crée un compte authentifié pour les tests. */
    private Compte creerCompte(RoleUtilisateur role) throws Exception {
        String email = "kanban-" + UUID.randomUUID() + "@test.local";
        String motDePasse = "MotDePasseTest123!";

        Utilisateur utilisateur = utilisateurs.save(new Utilisateur(
                email, passwordEncoder.encode(motDePasse), role));

        String reponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", email,
                                "motDePasse", motDePasse))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return new Compte(
                utilisateur.getId(),
                json.readTree(reponse).get("accessToken").asText());
    }

    /** Crée un dossier initialement dans la colonne A_TRAITER. */
    private String creerDossier(Compte admin, Compte responsable)
            throws Exception {
        String client = mvc.perform(post("/api/clients")
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Client Kanban\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String clientId = json.readTree(client).get("id").asText();

        String reponse = mvc.perform(post("/api/dossiers")
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "titre", "Dossier Kanban",
                                "description", "Description conservée",
                                "clientId", clientId,
                                "responsableId", responsable.id().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("A_TRAITER"))
                .andReturn().getResponse().getContentAsString();

        return json.readTree(reponse).get("id").asText();
    }

    /** Déplace un dossier avec le statut demandé. */
    private org.springframework.test.web.servlet.ResultActions deplacer(
            String id, Compte compte, String corps) throws Exception {
        return mvc.perform(patch("/api/dossiers/{id}/statut", id)
                .header("Authorization", compte.authorization())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps));
    }

    @Test
    void adminPeutDeplacerDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable);

        deplacer(id, admin, """
                {"statut":"EN_COURS"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.titre").value("Dossier Kanban"))
                .andExpect(jsonPath("$.description")
                        .value("Description conservée"));

        // Le changement doit être persisté et visible après relecture.
        mvc.perform(get("/api/dossiers/{id}", id)
                        .header("Authorization", admin.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));
    }

    @Test
    void responsablePeutDeplacerDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable);

        deplacer(id, responsable, """
                {"statut":"TERMINE"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINE"));
    }

    @Test
    void participantNePeutPasDeplacerDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte participant = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable);

        mvc.perform(put("/api/dossiers/{id}/participants", id)
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "participantIds",
                                java.util.List.of(participant.id())))))
                .andExpect(status().isOk());

        deplacer(id, participant, """
                {"statut":"EN_COURS"}
                """)
                .andExpect(status().isForbidden());
    }

    @Test
    void utilisateurNonAffecteRecoit404() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte inconnu = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable);

        deplacer(id, inconnu, """
                {"statut":"EN_COURS"}
                """)
                .andExpect(status().isNotFound());
    }

    @Test
    void statutInconnuEstRefuse() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable);

        deplacer(id, admin, """
                {"statut":"STATUT_INCONNU"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void statutAbsentEstRefuse() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable);

        deplacer(id, admin, "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void deplacementSansJwtEstRefuse() throws Exception {
        mvc.perform(patch("/api/dossiers/{id}/statut", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"statut":"EN_COURS"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
