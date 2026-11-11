package fr.superviseme.api.configuration.statut;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Vérifie le contrat HTTP de consultation des statuts personnalisés. */
@WebMvcTest(StatutPersonnaliseController.class)
class StatutPersonnaliseControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    StatutPersonnaliseService service;

    /** Une catégorie vide doit produire une liste JSON vide. */
    @Test
    @WithMockUser(roles = "ADMIN")
    void doitListerLesStatuts() throws Exception {
        when(service.lister(CategorieStatut.CLIENT)).thenReturn(List.of());

        mvc.perform(get("/api/admin/statuts")
                        .param("categorie", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
