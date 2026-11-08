package fr.superviseme.api.realisation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RealisationSecuriteTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void visiteurPeutConsulterLesRealisations() throws Exception {
        mvc.perform(get("/api/public/realisations"))
                .andExpect(status().isOk());
    }

    @Test
    void visiteurNePeutPasAjouterUneRealisation() throws Exception {
        mvc.perform(post("/api/realisations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void collaborateurNePeutPasAjouterUneRealisation() throws Exception {
        mvc.perform(post("/api/realisations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void collaborateurNePeutPasSupprimerUneRealisation() throws Exception {
        mvc.perform(delete(
                "/api/realisations/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isForbidden());
    }
}
