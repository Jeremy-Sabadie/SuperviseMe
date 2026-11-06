package fr.superviseme.api.client;

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
 * Tests d'intégration du référentiel clients.
 *
 * Vérifie les endpoints REST avec PostgreSQL et l'authentification JWT réelle.
 * Chaque test est isolé grâce à l'annulation de sa transaction.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientIntegrationTest {

    private static final String CLIENTS_API = "/api/clients";
    private static final String CLIENT_API = "/api/clients/{id}";
    private static final String MOT_DE_PASSE = "MotDePasseTest123!";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UtilisateurRepository utilisateurs;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper json;

    @Test
    void parcoursCrudComplet() throws Exception {
        // Arrange
        String token = obtenirToken();

        // Act : création
        String reponse = mvc.perform(post(CLIENTS_API)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nom": "Dupont",
                      "entreprise": "Atelier Dupont",
                      "email": "dupont@example.com",
                      "telephone": "0600000000",
                      "adresse": "Pau"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andReturn().getResponse().getContentAsString();

        String clientId = json.readTree(reponse).get("id").asText();

        // Assert : consultation
        mvc.perform(get(CLIENT_API, clientId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("dupont@example.com"));

        // Assert : recherche
        mvc.perform(get(CLIENTS_API)
                .param("recherche", "dup")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(clientId));

        // Act : modification
        mvc.perform(put(CLIENT_API, clientId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nom": "Dupont Modifié",
                      "email": "nouveau@example.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Dupont Modifié"));

        // Act : suppression
        mvc.perform(delete(CLIENT_API, clientId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        // Assert : client supprimé
        mvc.perform(get(CLIENT_API, clientId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void creationAvecNomVideEstRefusee() throws Exception {
        String token = obtenirToken();

        mvc.perform(post(CLIENTS_API)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nom": "   "}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creationAvecEmailInvalideEstRefusee() throws Exception {
        String token = obtenirToken();

        mvc.perform(post(CLIENTS_API)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nom": "Dupont",
                      "email": "adresse-invalide"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultationClientInexistantRetourne404() throws Exception {
        String token = obtenirToken();

        mvc.perform(get(CLIENT_API, UUID.randomUUID())
                .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }


    /**
     * Un client référencé par un dossier ne doit pas pouvoir être supprimé.
     */
    @Test
    void supprimerClientAvecDossierRetourneConflit() throws Exception {
        String token = obtenirToken();

        String reponseClient = mvc.perform(post(CLIENTS_API)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nom": "Client avec dossier"}
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String clientId = json.readTree(reponseClient).get("id").asText();

        mvc.perform(post("/api/dossiers")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "titre", "Dossier lié",
                        "responsableId", responsableId(token),
                        "clientId", clientId
                ))))
                .andExpect(status().isCreated());

        mvc.perform(delete(CLIENT_API, clientId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isConflict());

        mvc.perform(get(CLIENT_API, clientId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void accesSansJwtEstRefuse() throws Exception {
        mvc.perform(get(CLIENTS_API))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Crée un utilisateur ADMIN puis récupère son JWT par le parcours réel.
     */
    private String responsableId(String token) throws Exception {
        String reponse = mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(reponse).get("id").asText();
    }

    private String obtenirToken() throws Exception {
        String email = "client-" + UUID.randomUUID() + "@test.local";

        utilisateurs.save(new Utilisateur(
                email,
                passwordEncoder.encode(MOT_DE_PASSE),
                RoleUtilisateur.ADMIN
        ));

        String reponse = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", email,
                        "motDePasse", MOT_DE_PASSE
                ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(reponse).get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
