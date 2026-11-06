package fr.superviseme.api.entreprise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration de l'API REST de gestion des entreprises.
 *
 * Vérifie les autorisations d'accès, la validation des données
 * et les réponses HTTP des opérations de création et de consultation.
 *
 * Les utilisateurs simulés permettent de tester les rôles sans
 * dépendre du futur mécanisme d'émission des JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EntrepriseControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntrepriseRepository repository;

    /**
     * Un utilisateur non authentifié ne doit pas créer d'entreprise.
     */
    @Test
    void creationSansAuthentificationRefusee() throws Exception {
        mvc.perform(post("/api/admin/entreprises")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Entreprise test\"}"))
            .andExpect(status().isUnauthorized());
    }

    /**
     * Un utilisateur authentifié sans rôle ADMIN ne doit pas créer d'entreprise.
     */
    /**
     * Un administrateur client ne peut pas administrer
     * le registre central des entreprises.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void administrateurClientNePeutPasCreerEntreprise() throws Exception {
        mvc.perform(post("/api/admin/entreprises")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nom":"Entreprise interdite"}
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void creationSansRoleAdminInterdite() throws Exception {
        mvc.perform(post("/api/admin/entreprises")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Entreprise test\"}"))
            .andExpect(status().isForbidden());
    }

    /**
     * Un administrateur peut créer une entreprise.
     *
     * Une nouvelle entreprise doit automatiquement être placée
     * au statut EN_ATTENTE.
     */
    @Test
    @WithMockUser(roles = "OWNER")
    void adminPeutCreerEntreprise() throws Exception {
        mvc.perform(post("/api/admin/entreprises")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nom": "Entreprise test",
                      "email": "contact@example.com"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.nom").value("Entreprise test"))
            .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));
    }

    /**
     * Le nom d'une entreprise est obligatoire.
     *
     * Un nom composé uniquement d'espaces doit être refusé.
     */
    @Test
    @WithMockUser(roles = "OWNER")
    void nomVideRefuse() throws Exception {
        mvc.perform(post("/api/admin/entreprises")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\" \"}"))
            .andExpect(status().isBadRequest());
    }

    /**
     * Un administrateur peut consulter une entreprise existante.
     */
    @Test
    @WithMockUser(roles = "OWNER")
    void consultationEntrepriseExistante() throws Exception {
        Entreprise entreprise = repository.save(
            new Entreprise("Entreprise test", null, null));

        mvc.perform(get("/api/admin/entreprises/" + entreprise.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nom").value("Entreprise test"));
    }

    /**
     * La consultation d'une entreprise inexistante doit retourner HTTP 404.
     */
    @Test
    @WithMockUser(roles = "OWNER")
    void entrepriseInconnueRetourne404() throws Exception {
        mvc.perform(get("/api/admin/entreprises/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
