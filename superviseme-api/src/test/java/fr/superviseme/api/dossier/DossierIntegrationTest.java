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
 * Vérifie le parcours CRUD avec PostgreSQL et de vrais JWT.
 * Les changements en base sont annulés après chaque test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DossierIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UtilisateurRepository utilisateurs;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper json;

    /** Crée un utilisateur de test puis récupère son JWT via l'API. */
    private String responsableId(String token) throws Exception {
        String reponse = mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(reponse).get("id").asText();
    }

    private String obtenirToken() throws Exception {
        String email = "dossier-" + UUID.randomUUID() + "@test.local";
        String motDePasse = "MotDePasseTest123!";

        utilisateurs.save(new Utilisateur(
                email,
                passwordEncoder.encode(motDePasse),
                RoleUtilisateur.ADMIN
        ));

        String reponse = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", email,
                        "motDePasse", motDePasse
                ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return json.readTree(reponse).get("accessToken").asText();
    }

    /** Vérifie création, lecture, modification et suppression du dossier. */
    @Test
    void parcoursCrudComplet() throws Exception {
        String token = obtenirToken();
        String client = mvc.perform(post("/api/clients")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Client CRUD\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String clientId = json.readTree(client).get("id").asText();

        String creation = mvc.perform(post("/api/dossiers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "titre": "Dossier de test",
                      "description": "Création intégration",
                      "clientId": "%s",
                       "responsableId": "%s"
                    }
                    """.formatted(clientId, responsableId(token))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("A_TRAITER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = json.readTree(creation).get("id").asText();

        mvc.perform(get("/api/dossiers/{id}", id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Dossier de test"));

        mvc.perform(get("/api/dossiers")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(put("/api/dossiers/{id}", id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "titre": "Dossier modifié",
                      "description": "Modification intégration",
                      "statut": "EN_COURS",
                      "priorite": "HAUTE",
                      "echeance": "2026-12-15"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.priorite").value("HAUTE"));

        mvc.perform(delete("/api/dossiers/{id}", id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/dossiers/{id}", id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }


    /** Vérifie qu'un dossier peut être associé à un client existant. */
    @Test
    void creerDossierAvecClientExistant() throws Exception {
        String token = obtenirToken();

        String client = mvc.perform(post("/api/clients")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Client associé\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String clientId = json.readTree(client).get("id").asText();

        mvc.perform(post("/api/dossiers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "titre", "Dossier associé",
                        "responsableId", responsableId(token),
                        "clientId", clientId
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId));
    }

    /** Vérifie qu'un client inexistant ne peut pas être associé. */
    @Test
    void creerDossierAvecClientInexistantRetourne404() throws Exception {
        String token = obtenirToken();

        mvc.perform(post("/api/dossiers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "titre", "Dossier invalide",
                        "responsableId", responsableId(token),
                        "clientId", UUID.randomUUID().toString()
                ))))
                .andExpect(status().isNotFound());
    }


    /** Vérifie que PUT ne peut pas supprimer l'association client. */
    @Test
    void modifierDossierConserveAssociationClient() throws Exception {
        String token = obtenirToken();
        String auth = "Bearer " + token;

        String client = mvc.perform(post("/api/clients")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nom":"Client à dissocier"}
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String clientId = json.readTree(client).get("id").asText();

        String dossier = mvc.perform(post("/api/dossiers")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "titre", "Dossier à dissocier",
                        "responsableId", responsableId(token),
                        "clientId", clientId
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andReturn().getResponse().getContentAsString();

        String dossierId = json.readTree(dossier).get("id").asText();

        mvc.perform(put("/api/dossiers/{id}", dossierId)
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "titre": "Dossier à dissocier",
                      "statut": "A_TRAITER",
                      "priorite": "NORMALE",
                      "clientId": null
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(
                        clientId));

        mvc.perform(get("/api/dossiers/{id}", dossierId)
                .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(
                        clientId));

        mvc.perform(get("/api/clients/{id}", clientId)
                .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Client à dissocier"));
    }

    /** Vérifie qu'aucun dossier n'est accessible sans JWT. */
    @Test
    void accesSansJwtRefuse() throws Exception {
        mvc.perform(get("/api/dossiers"))
                .andExpect(status().isUnauthorized());
    }
}
