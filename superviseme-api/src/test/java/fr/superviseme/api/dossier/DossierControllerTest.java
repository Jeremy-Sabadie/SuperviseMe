package fr.superviseme.api.dossier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.superviseme.api.security.SecurityConfig;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests HTTP du contrôleur des dossiers.
 * Vérifie la validation, les permissions et les statuts de réponse.
 */
@WebMvcTest(DossierController.class)
@Import(SecurityConfig.class)
class DossierControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DossierService service;

    /** Un visiteur non connecté ne peut pas consulter les dossiers. */
    @Test
    void accesSansAuthentificationRefuse() throws Exception {
        mvc.perform(get("/api/dossiers"))
                .andExpect(status().isUnauthorized());
    }

    /** Un utilisateur authentifié peut consulter la liste. */
    @Test
    @WithMockUser(roles = "USER")
    void utilisateurPeutLister() throws Exception {
        when(service.lister()).thenReturn(java.util.List.of());

        mvc.perform(get("/api/dossiers"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    /** Un rôle non autorisé ne peut pas consulter les dossiers. */
    @Test
    @WithMockUser(roles = "OWNER")
    void ownerNePeutPasLister() throws Exception {
        mvc.perform(get("/api/dossiers"))
                .andExpect(status().isForbidden());
    }

    /** Une création sans titre doit être rejetée. */
    @Test
    @WithMockUser(roles = "ADMIN")
    void titreVideRefuse() throws Exception {
        mvc.perform(post("/api/dossiers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titre":"","description":"Test"}
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    /** Un dossier inexistant doit produire une réponse 404. */
    @Test
    @WithMockUser(roles = "ADMIN")
    void dossierInexistantRetourne404() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.consulter(id))
                .thenThrow(new DossierIntrouvableException(id));

        mvc.perform(get("/api/dossiers/{id}", id))
                .andExpect(status().isNotFound());
    }
}
