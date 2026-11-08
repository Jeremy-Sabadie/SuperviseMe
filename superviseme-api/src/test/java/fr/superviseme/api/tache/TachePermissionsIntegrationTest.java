package fr.superviseme.api.tache;

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
 * Vérifie les permissions réelles des tâches avec PostgreSQL et JWT.
 * Chaque test est transactionnel pour isoler ses données.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TachePermissionsIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UtilisateurRepository utilisateurs;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper json;

    private record Compte(UUID id, String token) {
        String authorization() {
            return "Bearer " + token;
        }
    }

    /**
     * Passe par l'API de connexion pour exercer la véritable chaîne JWT.
     */
    private Compte creerCompte(RoleUtilisateur role) throws Exception {
        String email = "taches-" + UUID.randomUUID() + "@test.local";
        String motDePasse = "MotDePasseTest123!";

        Utilisateur utilisateur = utilisateurs.save(
                new Utilisateur(
                        email,
                        passwordEncoder.encode(motDePasse),
                        role
                )
        );

        String reponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", email,
                                "motDePasse", motDePasse
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = json.readTree(reponse)
                .get("accessToken").asText();

        return new Compte(utilisateur.getId(), token);
    }

    private UUID creerTache(Compte admin, UUID responsableId) throws Exception {
        String reponse = mvc.perform(post("/api/taches")
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "titre", "Préparer le chantier",
                                "responsableId", responsableId.toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(json.readTree(reponse).get("id").asText());
    }

    @Test
    void administrateurPeutCreerEtLister() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        UUID id = creerTache(admin, responsable.id());

        mvc.perform(get("/api/taches")
                        .header("Authorization", admin.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id))
                        .exists());
    }

    @Test
    void utilisateurPeutListerLesTachesDeSesCollegues() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte collegue = creerCompte(RoleUtilisateur.USER);
        UUID id = creerTache(admin, responsable.id());

        mvc.perform(get("/api/taches")
                        .header("Authorization", collegue.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id))
                        .exists());
    }

    @Test
    void utilisateurNePeutPasCreer() throws Exception {
        Compte utilisateur = creerCompte(RoleUtilisateur.USER);

        mvc.perform(post("/api/taches")
                        .header("Authorization", utilisateur.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "titre", "Création interdite",
                                "responsableId", utilisateur.id().toString()
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void responsablePeutModifierSonStatut() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        UUID id = creerTache(admin, responsable.id());

        mvc.perform(patch("/api/taches/{id}/statut", id)
                        .header("Authorization", responsable.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"EN_COURS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));
    }

    @Test
    void collegueNePeutPasModifierLeStatut() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte collegue = creerCompte(RoleUtilisateur.USER);
        UUID id = creerTache(admin, responsable.id());

        mvc.perform(patch("/api/taches/{id}/statut", id)
                        .header("Authorization", collegue.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"TERMINEE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administrateurPeutModifierTouteTache() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        UUID id = creerTache(admin, responsable.id());

        mvc.perform(patch("/api/taches/{id}/statut", id)
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"TERMINEE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINEE"));
    }

    @Test
    void visiteurNonAuthentifieEstRefuse() throws Exception {
        mvc.perform(get("/api/taches"))
                .andExpect(status().isUnauthorized());
    }
}
