package fr.superviseme.api.demandecontact;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GestionDemandesSecuriteTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void visiteurNePeutPasListerLesDemandes() throws Exception {
        mvc.perform(get("/api/demandes-clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void visiteurNePeutPasLireLeCompteur() throws Exception {
        mvc.perform(get("/api/demandes-clients/compteur"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void visiteurNePeutPasModifierUnStatut() throws Exception {
        mvc.perform(patch(
                "/api/demandes-clients/00000000-0000-0000-0000-000000000001/statut")
                .contentType("application/json")
                .content("{\"statut\":\"TRAITE\"}"))
                .andExpect(status().isUnauthorized());
    }
}
