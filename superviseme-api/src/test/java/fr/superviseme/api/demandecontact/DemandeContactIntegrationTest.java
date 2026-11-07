package fr.superviseme.api.demandecontact;

import fr.superviseme.api.client.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration du formulaire de contact public.
 *
 * Vérifie la sécurité, la validation et la persistance PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DemandeContactIntegrationTest {

    private static final String API = "/api/public/contact";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClientRepository clients;

    @Autowired
    private DemandeContactRepository demandes;

    @Test
    void visiteurPeutEnvoyerUneDemandeSansConnexion() throws Exception {
        // Arrange
        long nombreInitial = demandes.count();

        // Act
        mvc.perform(post(API)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nom": "Martin",
                      "email": "martin-us010@example.com",
                      "motif": "Demande de devis",
                      "message": "Fabrication d'un escalier"
                    }
                    """))
                .andExpect(status().isCreated());

        // Assert
        assertEquals(nombreInitial + 1, demandes.count());
        assertTrue(clients.findByEmailIgnoreCase(
                "martin-us010@example.com").isPresent());
    }

    @Test
    void emailInvalideEstRefuse() throws Exception {
        mvc.perform(post(API)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nom": "Martin",
                      "email": "email-invalide",
                      "motif": "Devis",
                      "message": "Fabrication d'un escalier"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void messageVideEstRefuse() throws Exception {
        mvc.perform(post(API)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nom": "Martin",
                      "email": "martin@example.com",
                      "motif": "Devis",
                      "message": " "
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void visiteurNePeutPasConsulterLesClients() throws Exception {
        mvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized());
    }
}
